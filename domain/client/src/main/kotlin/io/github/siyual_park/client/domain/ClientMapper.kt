package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.client.entity.ClientData
import io.github.siyual_park.client.repository.ClientCredentialRepository
import io.github.siyual_park.client.repository.ClientRepository
import io.github.siyual_park.client.repository.ClientScopeRepository
import io.github.siyual_park.data.aggregation.FetchContextProvider
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator

@Component
class ClientMapper(
    private val clientRepository: ClientRepository,
    private val clientCredentialRepository: ClientCredentialRepository,
    private val clientScopeRepository: ClientScopeRepository,
    private val scopeTokenStorage: ScopeTokenStorage,
    private val operator: TransactionalOperator,
    private val eventPublisher: EventPublisher
) : Mapper<ClientData, Client> {
    override val sourceType = object : TypeReference<ClientData>() {}
    override val targetType = object : TypeReference<Client>() {}

    override suspend fun map(source: ClientData): Client {
        val fetchContextProvider = FetchContextProvider()

        return Client(
            source,
            clientRepository,
            clientCredentialRepository,
            clientScopeRepository,
            scopeTokenStorage,
            fetchContextProvider,
            operator,
            eventPublisher
        )
    }
}
