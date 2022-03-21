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
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.persistence.Persistence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.net.URL
import java.time.Instant

class Client(
    value: ClientData,
    clientRepository: ClientRepository,
    private val clientCredentialRepository: ClientCredentialRepository,
    private val clientScopeRepository: ClientScopeRepository,
    private val scopeTokenStorage: ScopeTokenStorage,
    private val operator: TransactionalOperator,
    private val eventPublisher: EventPublisher
) : Persistence<ClientData, Long>(value, clientRepository, eventPublisher), ClientEntity, Authorizable {
    val id: Long
        get() = root[ClientData::id] ?: throw EmptyResultDataAccessException(1)

    override val clientId
        get() = root[ClientData::id]

    var name: String
        get() = root[ClientData::name]
        set(value) { root[ClientData::name] = value }

    val type: ClientType
        get() = root[ClientData::type]

    var origin: URL
        get() = root[ClientData::origin]
        set(value) { root[ClientData::origin] = value }

    private var credential: ClientCredential? = null

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
        clientScopeRepository.deleteAll(
            where(ClientScopeData::clientId).`is`(id)
                .and(where(ClientScopeData::scopeTokenId).`is`(scopeToken.id))
        )
    }

    suspend fun getCredential(): ClientCredential {
        val credential = credential
        if (credential != null) {
            credential.link()
            return credential
        }

        return clientCredentialRepository.findByClientIdOrFail(id)
            .let { ClientCredential(it, clientCredentialRepository, eventPublisher) }
            .also { it.link() }
            .also { this.credential = it }
    }

    fun getScope(): Flow<ScopeToken> {
        return flow {
            val scopeTokenIds = clientScopeRepository.findAllByClientId(id)
                .map { it.scopeTokenId }
                .toList()

            scopeTokenStorage.load(scopeTokenIds)
                .collect { emitAll(it.resolve()) }
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
        root.clear()
        credential = null

        operator.executeAndAwait {
            clientScopeRepository.deleteAllByClientId(id)
            clientCredentialRepository.deleteByClientId(id)
            root[ClientData::deletedAt] = Instant.now()

            sync()
        }
    }
}
