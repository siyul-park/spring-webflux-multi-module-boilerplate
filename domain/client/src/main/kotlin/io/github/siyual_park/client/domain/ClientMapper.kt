package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.client.entity.ClientData
import io.github.siyual_park.client.repository.ClientDataRepository
import io.github.siyual_park.client.repository.ClientScopeDataRepository
import io.github.siyual_park.data.aggregation.FetchContext
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import org.springframework.stereotype.Component

@Component
class ClientMapper(
    private val clientDataRepository: ClientDataRepository,
    private val clientScopeDataRepository: ClientScopeDataRepository,
    private val scopeTokenStorage: ScopeTokenStorage
) : Mapper<ClientData, Client> {
    override val sourceType = object : TypeReference<ClientData>() {}
    override val targetType = object : TypeReference<Client>() {}

    override suspend fun map(source: ClientData): Client {
        val fetchContext = FetchContext()

        return Client(
            source,
            clientDataRepository,
            clientScopeDataRepository,
            scopeTokenStorage,
            fetchContext
        )
    }
}
