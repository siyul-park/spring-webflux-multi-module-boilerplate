package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.authorization.Authorizable
import io.github.siyual_park.auth.domain.hash
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.client.entity.ClientEntity
import io.github.siyual_park.data.aggregation.FetchContext
import io.github.siyual_park.data.aggregation.get
import io.github.siyual_park.data.criteria.and
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.data.repository.findOneOrFail
import io.github.siyual_park.persistence.Persistence
import io.github.siyual_park.persistence.PersistenceSynchronization
import io.github.siyual_park.persistence.proxy
import io.github.siyual_park.ulid.ULID
import io.github.siyual_park.user.domain.auth.UserPrincipal
import io.github.siyual_park.user.entity.UserData
import io.github.siyual_park.user.entity.UserEntity
import io.github.siyual_park.user.entity.UserScopeData
import io.github.siyual_park.user.repository.UserDataRepository
import io.github.siyual_park.user.repository.UserScopeDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.security.MessageDigest

class User(
    value: UserData,
    userDataRepository: UserDataRepository,
    private val userScopeDataRepository: UserScopeDataRepository,
    private val scopeTokenStorage: ScopeTokenStorage,
    fetchContext: FetchContext
) : Persistence<UserData, ULID>(value, userDataRepository), UserEntity, Authorizable {
    val id by proxy(root, UserData::id)
    var email by proxy(root, UserData::email)
    var name by proxy(root, UserData::name)

    val createdAt by proxy(root, UserData::createdAt)
    val updatedAt by proxy(root, UserData::updatedAt)

    override val userId by proxy(root, UserData::id)

    private val hashAlgorithm by proxy(root, UserData::hashAlgorithm)

    private val scopeContext = fetchContext.get(userScopeDataRepository)
    private val scopeFetcher = scopeContext.join(
        where(UserScopeData::userId).`is`(userId)
    )

    init {
        synchronize(
            object : PersistenceSynchronization {
                override suspend fun beforeClear() {
                    scopeFetcher.clear()
                    userScopeDataRepository.deleteAllByUserId(id)
                }
            }
        )
    }

    fun isPassword(password: String): Boolean {
        return root[UserData::password] == encode(password)
    }

    fun setPassword(password: String) {
        root[UserData::password] = encode(password)
    }

    override suspend fun has(scopeToken: ScopeToken): Boolean {
        return userScopeDataRepository.exists(
            where(UserScopeData::userId).`is`(id)
                .and(where(UserScopeData::scopeTokenId).`is`(scopeToken.id))
        )
    }

    override suspend fun grant(scopeToken: ScopeToken) {
        userScopeDataRepository.create(
            UserScopeData(
                userId = id,
                scopeTokenId = scopeToken.id
            )
        ).also { scopeContext.clear(it) }
    }

    override suspend fun revoke(scopeToken: ScopeToken) {
        val userScope = userScopeDataRepository.findOneOrFail(
            where(UserScopeData::userId).`is`(id)
                .and(where(UserScopeData::scopeTokenId).`is`(scopeToken.id))
        )
        scopeContext.clear(userScope)
        userScopeDataRepository.delete(userScope)
    }

    fun getScope(deep: Boolean = true): Flow<ScopeToken> {
        return flow {
            val scopeTokenIds = scopeFetcher.fetch()
                .map { it.scopeTokenId }
                .toList()

            scopeTokenStorage.load(scopeTokenIds)
                .collect {
                    if (deep) {
                        emitAll(it.resolve())
                    } else {
                        emit(it)
                    }
                }
        }
    }

    suspend fun toPrincipal(
        clientEntity: ClientEntity? = null,
        push: List<ScopeToken> = emptyList(),
        pop: List<ScopeToken> = emptyList()
    ): UserPrincipal {
        val myScope = getScope().toList()
        val scope = mutableSetOf<ScopeToken>()

        scope.addAll(myScope.filter { token -> pop.firstOrNull { it.id == token.id } == null })
        scope.addAll(push.toList())

        return UserPrincipal(
            id = id,
            userId = userId,
            clientId = clientEntity?.clientId,
            scope = scope.toSet()
        )
    }

    private fun encode(password: String): String {
        val messageDigest = MessageDigest.getInstance(hashAlgorithm)
        return messageDigest.hash(password)
    }
}
