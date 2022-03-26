package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.authorization.Authorizable
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.client.entity.ClientEntity
import io.github.siyual_park.data.event.AfterDeleteEvent
import io.github.siyual_park.data.event.BeforeDeleteEvent
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.persistence.AsyncLazy
import io.github.siyual_park.persistence.Persistence
import io.github.siyual_park.persistence.proxy
import io.github.siyual_park.persistence.proxyNotNull
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
import kotlinx.coroutines.flow.toSet
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

class User(
    value: UserData,
    private val userRepository: UserRepository,
    private val userCredentialRepository: UserCredentialRepository,
    private val userScopeRepository: UserScopeRepository,
    private val scopeTokenStorage: ScopeTokenStorage,
    private val operator: TransactionalOperator,
    private val eventPublisher: EventPublisher
) : Persistence<UserData, Long>(value, userRepository, eventPublisher), UserEntity, Authorizable {
    val id: Long by proxyNotNull(root, UserData::id)
    override val userId by proxyNotNull(root, UserData::id)
    var name by proxy(root, UserData::name)
    var email by proxy(root, UserData::email)

    private val credential = AsyncLazy {
        UserCredential(
            userCredentialRepository.findByUserIdOrFail(id),
            userCredentialRepository,
            eventPublisher
        )
    }

    override suspend fun has(scopeToken: ScopeToken): Boolean {
        val scope = getScope().toSet()
        return scope.contains(scopeToken)
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

    override suspend fun clear() {
        operator.executeAndAwait {
            eventPublisher.publish(BeforeDeleteEvent(this))
            userScopeRepository.deleteAllByUserId(id)
            credential.get().clear()
            credential.clear()
            userRepository.delete(root.raw())
            root.clear()
            eventPublisher.publish(AfterDeleteEvent(this))
        }
    }
}
