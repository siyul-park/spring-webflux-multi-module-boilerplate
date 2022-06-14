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
import io.github.siyual_park.data.aggregation.FetchContextProvider
import io.github.siyual_park.data.aggregation.get
import io.github.siyual_park.data.cache.SuspendLazy
import io.github.siyual_park.data.criteria.and
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.data.repository.findOneOrFail
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.persistence.Persistence
import io.github.siyual_park.persistence.PersistencePropagateSynchronization
import io.github.siyual_park.persistence.PersistenceSynchronization
import io.github.siyual_park.persistence.proxy
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.transaction.reactive.TransactionalOperator

class Client(
    value: ClientData,
    clientRepository: ClientRepository,
    private val clientCredentialRepository: ClientCredentialRepository,
    private val clientScopeRepository: ClientScopeRepository,
    private val scopeTokenStorage: ScopeTokenStorage,
    fetchContextProvider: FetchContextProvider,
    operator: TransactionalOperator,
    private val eventPublisher: EventPublisher
) : Persistence<ClientData, ULID>(
    value,
    clientRepository,
    operator,
    eventPublisher
),
    ClientEntity,
    Authorizable {
    val id by proxy(root, ClientData::id)
    var name by proxy(root, ClientData::name)
    val type by proxy(root, ClientData::type)
    var origin by proxy(root, ClientData::origin)

    override val clientId by proxy(root, ClientData::id)

    private val scopeContext = fetchContextProvider.get(clientScopeRepository)
    private val scopeFetcher = scopeContext.join(
        where(ClientScopeData::clientId).`is`(clientId)
    )

    private val credential = SuspendLazy {
        ClientCredential(
            clientCredentialRepository.findByClientIdOrFail(clientId),
            clientCredentialRepository,
            eventPublisher
        ).also {
            synchronize(PersistencePropagateSynchronization(it))
        }
    }

    init {
        synchronize(
            object : PersistenceSynchronization {
                override suspend fun beforeClear() {
                    scopeFetcher.clear()

                    clientScopeRepository.deleteAllByClientId(id)
                    if (isConfidential()) {
                        credential.get().clear()
                    }
                }

                override suspend fun afterClear() {
                    credential.clear()
                }
            }
        )
    }

    fun isConfidential(): Boolean {
        return type == ClientType.CONFIDENTIAL
    }

    fun isPublic(): Boolean {
        return type == ClientType.PUBLIC
    }

    override suspend fun has(scopeToken: ScopeToken): Boolean {
        return clientScopeRepository.exists(
            where(ClientScopeData::clientId).`is`(id)
                .and(where(ClientScopeData::scopeTokenId).`is`(scopeToken.id))
        )
    }

    override suspend fun grant(scopeToken: ScopeToken) {
        clientScopeRepository.create(
            ClientScopeData(
                clientId = id,
                scopeTokenId = scopeToken.id
            )
        ).also { scopeContext.clear(it) }
    }

    override suspend fun revoke(scopeToken: ScopeToken) {
        val clientScope = clientScopeRepository.findOneOrFail(
            where(ClientScopeData::clientId).`is`(id)
                .and(where(ClientScopeData::scopeTokenId).`is`(scopeToken.id))
        )
        scopeContext.clear(clientScope)
        clientScopeRepository.delete(clientScope)
    }

    suspend fun getCredential(): ClientCredential {
        return credential.get()
            .also { it.link() }
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
            id = id,
            clientId = clientId,
            scope = scope.toSet()
        )
    }
}
