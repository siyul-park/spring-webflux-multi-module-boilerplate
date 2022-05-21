package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.authorization.Authorizable
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.client.entity.ClientEntity
import io.github.siyual_park.data.cache.AsyncLazy
import io.github.siyual_park.data.repository.r2dbc.findOneOrFail
import io.github.siyual_park.data.repository.r2dbc.where
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.persistence.Persistence
import io.github.siyual_park.persistence.PersistencePropagateSynchronization
import io.github.siyual_park.persistence.PersistenceSynchronization
import io.github.siyual_park.persistence.proxy
import io.github.siyual_park.ulid.ULID
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.transaction.reactive.TransactionalOperator

class User(
    value: UserData,
    userRepository: UserRepository,
    private val userCredentialRepository: UserCredentialRepository,
    private val userScopeRepository: UserScopeRepository,
    private val scopeTokenStorage: ScopeTokenStorage,
    operator: TransactionalOperator,
    private val eventPublisher: EventPublisher
) : Persistence<UserData, ULID>(
    value,
    userRepository,
    operator,
    eventPublisher
),
    UserEntity,
    Authorizable {
    val id by proxy(root, UserData::id)
    var email by proxy(root, UserData::email)
    var name by proxy(root, UserData::name)

    override val userId by proxy(root, UserData::id)

    init {
        synchronize(
            object : PersistenceSynchronization {
                override suspend fun beforeClear() {
                    userScopeRepository.deleteAllByUserId(id)
                    credential.get().clear()
                }

                override suspend fun afterClear() {
                    credential.clear()
                }
            }
        )
    }

    private val credential = AsyncLazy {
        UserCredential(
            userCredentialRepository.findByUserIdOrFail(id),
            userCredentialRepository,
            eventPublisher
        ).also {
            synchronize(PersistencePropagateSynchronization(it))
        }
    }

    override suspend fun has(scopeToken: ScopeToken): Boolean {
        return userScopeRepository.exists(
            where(UserScopeData::userId).`is`(id)
                .and(where(UserScopeData::scopeTokenId).`is`(scopeToken.id))
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
        val userScope = userScopeRepository.findOneOrFail(
            where(UserScopeData::userId).`is`(id)
                .and(where(UserScopeData::scopeTokenId).`is`(scopeToken.id))
        )
        userScopeRepository.delete(userScope)
    }

    suspend fun getCredential(): UserCredential {
        return credential.get()
            .also { it.link() }
    }

    fun getScope(deep: Boolean = true): Flow<ScopeToken> {
        return flow {
            val scopeTokenIds = userScopeRepository.findAllByUserId(id)
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
}
