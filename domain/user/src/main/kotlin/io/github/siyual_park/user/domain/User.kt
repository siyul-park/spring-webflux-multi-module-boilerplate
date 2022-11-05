package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.authorization.Authorizable
import io.github.siyual_park.auth.domain.hash
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.client.entity.ClientAssociable
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
import io.github.siyual_park.user.entity.UserAssociable
import io.github.siyual_park.user.entity.UserEntity
import io.github.siyual_park.user.entity.UserScopeEntity
import io.github.siyual_park.user.repository.UserEntityRepository
import io.github.siyual_park.user.repository.UserScopeEntityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.security.MessageDigest

class User(
    entity: UserEntity,
    userEntityRepository: UserEntityRepository,
    private val userScopeEntityRepository: UserScopeEntityRepository,
    private val scopeTokenStorage: ScopeTokenStorage,
    fetchContext: FetchContext
) : Persistence<UserEntity, ULID>(entity, userEntityRepository), UserAssociable, Authorizable {
    val id by proxy(root, UserEntity::id)
    var email by proxy(root, UserEntity::email)
    var name by proxy(root, UserEntity::name)

    val createdAt by proxy(root, UserEntity::createdAt)
    val updatedAt by proxy(root, UserEntity::updatedAt)

    override val userId by proxy(root, UserEntity::id)

    private val hashAlgorithm by proxy(root, UserEntity::hashAlgorithm)

    private val scopeContext = fetchContext.get(userScopeEntityRepository)
    private val scopeFetcher = scopeContext.join(
        where(UserScopeEntity::userId).`is`(userId)
    )

    init {
        synchronize(
            object : PersistenceSynchronization {
                override suspend fun beforeClear() {
                    scopeFetcher.clear()
                    userScopeEntityRepository.deleteAllByUserId(id)
                }
            }
        )
    }

    fun isPassword(password: String): Boolean {
        return root[UserEntity::password] == encode(password)
    }

    fun setPassword(password: String) {
        root[UserEntity::password] = encode(password)
    }

    override suspend fun has(scopeToken: ScopeToken): Boolean {
        return userScopeEntityRepository.exists(
            where(UserScopeEntity::userId).`is`(id)
                .and(where(UserScopeEntity::scopeTokenId).`is`(scopeToken.id))
        )
    }

    override suspend fun grant(scopeToken: ScopeToken) {
        userScopeEntityRepository.create(
            UserScopeEntity(
                userId = id,
                scopeTokenId = scopeToken.id
            )
        ).also { scopeContext.clear(it) }
    }

    override suspend fun revoke(scopeToken: ScopeToken) {
        val userScope = userScopeEntityRepository.findOneOrFail(
            where(UserScopeEntity::userId).`is`(id)
                .and(where(UserScopeEntity::scopeTokenId).`is`(scopeToken.id))
        )
        scopeContext.clear(userScope)
        userScopeEntityRepository.delete(userScope)
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
        client: ClientAssociable? = null,
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
            clientId = client?.clientId,
            scope = scope.toSet()
        )
    }

    private fun encode(password: String): String {
        val messageDigest = MessageDigest.getInstance(hashAlgorithm)
        return messageDigest.hash(password)
    }
}
