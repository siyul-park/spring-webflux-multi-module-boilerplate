package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.client.entity.ClientData
import io.github.siyual_park.client.repository.ClientCredentialDataRepository
import io.github.siyual_park.client.repository.ClientDataRepository
import io.github.siyual_park.client.repository.ClientScopeDataRepository
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.persistence.QueryStorage
import io.github.siyual_park.persistence.SimpleQueryStorage
import io.github.siyual_park.ulid.ULID
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Component

@Component
class ClientStorage(
    private val clientDataRepository: ClientDataRepository,
    private val clientCredentialDataRepository: ClientCredentialDataRepository,
    private val clientScopeDataRepository: ClientScopeDataRepository,
    private val scopeTokenStorage: ScopeTokenStorage
) : QueryStorage<Client, ULID> by SimpleQueryStorage(
    clientDataRepository,
    ClientMapper(clientDataRepository, clientCredentialDataRepository, clientScopeDataRepository, scopeTokenStorage).let { mapper -> { mapper.map(it) } },
    ClientsMapper(clientDataRepository, clientCredentialDataRepository, clientScopeDataRepository, scopeTokenStorage).let { mapper -> { mapper.map(it) } },
) {
    suspend fun load(name: String): Client? {
        return load(where(ClientData::name).`is`(name))
    }
}

suspend fun ClientStorage.loadOrFail(name: String): Client {
    return load(name) ?: throw EmptyResultDataAccessException(1)
}
