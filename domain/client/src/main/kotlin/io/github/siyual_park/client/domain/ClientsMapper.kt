package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.client.entity.ClientEntity
import io.github.siyual_park.client.repository.ClientEntityRepository
import io.github.siyual_park.client.repository.ClientScopeEntityRepository
import io.github.siyual_park.data.aggregation.FetchContext
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import org.springframework.stereotype.Component

@Component
class ClientsMapper(
    private val clientEntityRepository: ClientEntityRepository,
    private val clientScopeEntityRepository: ClientScopeEntityRepository,
    private val scopeTokenStorage: ScopeTokenStorage
) : Mapper<Collection<ClientEntity>, Collection<Client>> {
    override val sourceType = object : TypeReference<Collection<ClientEntity>>() {}
    override val targetType = object : TypeReference<Collection<Client>>() {}

    override suspend fun map(source: Collection<ClientEntity>): Collection<Client> {
        val fetchContext = FetchContext()
        return source.map {
            Client(
                it,
                clientEntityRepository,
                clientScopeEntityRepository,
                scopeTokenStorage,
                fetchContext
            )
        }
    }
}
