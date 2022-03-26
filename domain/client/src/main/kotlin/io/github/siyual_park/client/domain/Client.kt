package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.authorization.Authorizable
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.client.domain.auth.ClientPrincipal
import io.github.siyual_park.client.entity.ClientData
import io.github.siyual_park.client.entity.ClientEntity
import io.github.siyual_park.client.entity.ClientScopeData
import io.github.siyual_park.client.entity.ClientType
import io.github.siyual_park.client.repository.ClientCredentialRepository
import io.github.siyual_park.client.repository.ClientRepository
import io.github.siyual_park.client.repository.ClientScopeRepository
import io.github.siyual_park.data.event.AfterDeleteEvent
import io.github.siyual_park.data.event.BeforeDeleteEvent
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.repository.r2dbc.findOneOrFail
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.persistence.AsyncLazy
import io.github.siyual_park.persistence.Persistence
import io.github.siyual_park.persistence.proxy
import io.github.siyual_park.persistence.proxyNotNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

class Client(
    value: ClientData,
    private val clientRepository: ClientRepository,
    private val clientCredentialRepository: ClientCredentialRepository,
    private val clientScopeRepository: ClientScopeRepository,
    private val scopeTokenStorage: ScopeTokenStorage,
    private val operator: TransactionalOperator,
    private val eventPublisher: EventPublisher
) : Persistence<ClientData, Long>(value, clientRepository, eventPublisher), ClientEntity, Authorizable {
    val id: Long by proxyNotNull(root, ClientData::id)
    override val clientId by proxyNotNull(root, ClientData::id)
    var name by proxy(root, ClientData::name)
    val type by proxy(root, ClientData::type)
    var origin by proxy(root, ClientData::origin)

    private val credential = AsyncLazy {
        ClientCredential(
            clientCredentialRepository.findByClientIdOrFail(id),
            clientCredentialRepository,
            eventPublisher
        )
    }

    fun isConfidential(): Boolean {
        return type == ClientType.CONFIDENTIAL
    }

    fun isPublic(): Boolean {
        return type == ClientType.PUBLIC
    }

    override suspend fun has(scopeToken: ScopeToken): Boolean {
        val scope = getScope().toSet()
        return scope.contains(scopeToken)
    }

    override suspend fun grant(scopeToken: ScopeToken) {
        clientScopeRepository.create(
            ClientScopeData(
                clientId = id,
                scopeTokenId = scopeToken.id
            )
        )
    }

    override suspend fun revoke(scopeToken: ScopeToken) {
        val clientScope = clientScopeRepository.findOneOrFail(
            where(ClientScopeData::clientId).`is`(id)
                .and(where(ClientScopeData::scopeTokenId).`is`(scopeToken.id))
        )
        clientScopeRepository.delete(clientScope)
    }

    suspend fun getCredential(): ClientCredential {
        return credential.get()
            .also { it.link() }
    }

    fun getScope(deep: Boolean = true): Flow<ScopeToken> {
        return flow {
            val scopeTokenIds = clientScopeRepository.findAllByClientId(id)
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
        push: List<ScopeToken> = emptyList(),
        pop: List<ScopeToken> = emptyList()
    ): ClientPrincipal {
        val myScope = getScope()
        val scope = mutableSetOf<ScopeToken>()

        scope.addAll(
            myScope
                .filter { token -> pop.firstOrNull { it.id == token.id } == null }
                .toList()
        )
        scope.addAll(push.toList())

        return ClientPrincipal(
            id = clientId.toString(),
            scope = scope.toSet()
        )
    }

    override suspend fun clear() {
        operator.executeAndAwait {
            eventPublisher.publish(BeforeDeleteEvent(this))
            clientScopeRepository.deleteAllByClientId(id)
            if (isConfidential()) {
                credential.get().clear()
                credential.clear()
            }
            clientRepository.delete(root.raw())
            root.clear()
            eventPublisher.publish(AfterDeleteEvent(this))
        }
    }
}
