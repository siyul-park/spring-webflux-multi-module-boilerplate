package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.authorization.Authorizable
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.client.entity.ClientEntity
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.persistence.Persistence
import io.github.siyual_park.user.domain.auth.UserPrincipal
import io.github.siyual_park.user.entity.UserData
import io.github.siyual_park.user.entity.UserEntity
import io.github.siyual_park.user.entity.UserScopeData
import io.github.siyual_park.user.repository.UserCredentialRepository
import io.github.siyual_park.user.repository.UserRepository
import io.github.siyual_park.user.repository.UserScopeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.Instant

class User(
    value: UserData,
    userRepository: UserRepository,
    private val userCredentialRepository: UserCredentialRepository,
    private val userScopeRepository: UserScopeRepository,
    private val scopeTokenStorage: ScopeTokenStorage,
    private val operator: TransactionalOperator,
) : Persistence<UserData, Long>(value, userRepository), UserEntity, Authorizable {
    val id: Long
        get() = root[UserData::id] ?: throw EmptyResultDataAccessException(1)

    override val userId
        get() = root[UserData::id]

    var name: String
        get() = root[UserData::name]
        set(value) { root[UserData::name] = value }

    private var credential: UserCredential? = null

    override suspend fun clear() {
        root.clear()

        operator.executeAndAwait {
            userScopeRepository.deleteAllByUserId(id)
            userCredentialRepository.deleteByUserId(id)
            root[UserData::deletedAt] = Instant.now()

            sync()
        }
    }

    suspend fun toPrincipal(
        clientEntity: ClientEntity? = null,
        push: List<ScopeToken> = emptyList(),
        pop: List<ScopeToken> = emptyList()
    ): UserPrincipal {
        val myScope = getResolvedScope().toList()
        val scope = mutableSetOf<ScopeToken>()

        scope.addAll(
            myScope.filter { token -> pop.firstOrNull { it.id == token.id } == null }
        )
        scope.addAll(push.toList())

        return UserPrincipal(
            id = userId.toString(),
            clientId = clientEntity?.clientId,
            scope = scope.toSet()
        )
    }

    override suspend fun grant(scopeToken: ScopeToken) {
        userScopeRepository.create(
            UserScopeData(
                userId = id,
                scopeTokenId = scopeToken.id
            )
        )
    }

    override suspend fun revoke(scopeToken: ScopeToken) {
        userScopeRepository.deleteAll(
            where(UserScopeData::userId).`is`(id)
                .and(where(UserScopeData::scopeTokenId).`is`(scopeToken.id))
        )
    }

    suspend fun getCredential(): UserCredential {
        val credential = credential
        if (credential != null) {
            credential.link()
            return credential
        }

        return userCredentialRepository.findByUserIdOrFail(id)
            .let { UserCredential(it, userCredentialRepository) }
            .also { it.link() }
            .also { this.credential = it }
    }

    fun getResolvedScope(): Flow<ScopeToken> {
        return flow {
            getScope()
                .collect { emitAll(it.resolve()) }
        }
    }

    fun getScope(): Flow<ScopeToken> {
        return flow {
            val scopeTokenIds = userScopeRepository.findAllByUserId(id)
                .map { it.scopeTokenId }
                .toList()

            scopeTokenStorage.load(scopeTokenIds)
                .collect { emit(it) }
        }
    }
}
