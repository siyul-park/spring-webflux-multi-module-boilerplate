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
class ClientsMapper(
    private val clientDataRepository: ClientDataRepository,
    private val clientScopeDataRepository: ClientScopeDataRepository,
    private val scopeTokenStorage: ScopeTokenStorage
) : Mapper<Collection<ClientData>, Collection<Client>> {
    override val sourceType = object : TypeReference<Collection<ClientData>>() {}
    override val targetType = object : TypeReference<Collection<Client>>() {}

    override suspend fun map(source: Collection<ClientData>): Collection<Client> {
        val fetchContext = FetchContext()
        return source.map {
            Client(
                it,
                clientDataRepository,
                clientScopeDataRepository,
                scopeTokenStorage,
                fetchContext
            )
        }
    }
}
