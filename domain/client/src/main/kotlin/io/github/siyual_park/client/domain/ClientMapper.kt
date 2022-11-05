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
class ClientMapper(
    private val clientEntityRepository: ClientEntityRepository,
    private val clientScopeEntityRepository: ClientScopeEntityRepository,
    private val scopeTokenStorage: ScopeTokenStorage
) : Mapper<ClientEntity, Client> {
    override val sourceType = object : TypeReference<ClientEntity>() {}
    override val targetType = object : TypeReference<Client>() {}

    override suspend fun map(source: ClientEntity): Client {
        val fetchContext = FetchContext()

        return Client(
            source,
            clientEntityRepository,
            clientScopeEntityRepository,
            scopeTokenStorage,
            fetchContext
        )
    }
}
