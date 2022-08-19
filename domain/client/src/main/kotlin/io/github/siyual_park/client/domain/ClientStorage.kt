package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.client.entity.ClientData
import io.github.siyual_park.client.repository.ClientCredentialRepository
import io.github.siyual_park.client.repository.ClientRepository
import io.github.siyual_park.client.repository.ClientScopeRepository
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.persistence.QueryStorage
import io.github.siyual_park.persistence.SimpleQueryStorage
import io.github.siyual_park.ulid.ULID
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator

@Component
class ClientStorage(
    private val clientRepository: ClientRepository,
    private val clientCredentialRepository: ClientCredentialRepository,
    private val clientScopeRepository: ClientScopeRepository,
    private val scopeTokenStorage: ScopeTokenStorage,
    private val operator: TransactionalOperator,
    private val eventPublisher: EventPublisher
) : QueryStorage<Client, ULID> by SimpleQueryStorage(
    clientRepository,
    ClientMapper(clientRepository, clientCredentialRepository, clientScopeRepository, scopeTokenStorage, operator, eventPublisher).let { mapper -> { mapper.map(it) } },
    ClientsMapper(clientRepository, clientCredentialRepository, clientScopeRepository, scopeTokenStorage, operator, eventPublisher).let { mapper -> { mapper.map(it) } },
) {
    suspend fun load(name: String): Client? {
        return load(where(ClientData::name).`is`(name))
    }
}

suspend fun ClientStorage.loadOrFail(name: String): Client {
    return load(name) ?: throw EmptyResultDataAccessException(1)
}
