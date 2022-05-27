package io.github.siyual_park.client.domain

import com.google.common.cache.CacheBuilder
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
import java.time.Duration

@Component
class ClientsMapper(
    private val clientRepository: ClientRepository,
    private val clientCredentialRepository: ClientCredentialRepository,
    private val clientScopeRepository: ClientScopeRepository,
    private val scopeTokenStorage: ScopeTokenStorage,
    private val operator: TransactionalOperator,
    private val eventPublisher: EventPublisher
) : Mapper<List<ClientData>, List<Client>> {
    override val sourceType = object : TypeReference<List<ClientData>>() {}
    override val targetType = object : TypeReference<List<Client>>() {}

    override suspend fun map(source: List<ClientData>): List<Client> {
        val fetchContextProvider = FetchContextProvider {
            CacheBuilder.newBuilder()
                .weakKeys()
                .expireAfterWrite(Duration.ofSeconds(5))
        }

        return source.map {
            Client(
                it,
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
}
