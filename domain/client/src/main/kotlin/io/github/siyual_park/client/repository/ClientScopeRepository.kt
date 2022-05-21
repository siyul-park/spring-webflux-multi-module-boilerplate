package io.github.siyual_park.client.repository

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.client.entity.ClientScopeData
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepositoryBuilder
import io.github.siyual_park.data.repository.r2dbc.where
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class ClientScopeRepository(
    entityOperations: R2dbcEntityOperations,
    eventPublisher: EventPublisher? = null
) : R2DBCRepository<ClientScopeData, Long> by R2DBCRepositoryBuilder<ClientScopeData, Long>(entityOperations, ClientScopeData::class)
    .enableEvent(eventPublisher)
    .enableCache({
        CacheBuilder.newBuilder()
            .softValues()
            .expireAfterAccess(Duration.ofMinutes(2))
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(1_000)
    })
    .enableQueryCache({
        CacheBuilder.newBuilder()
            .softValues()
            .expireAfterAccess(Duration.ofSeconds(1))
            .maximumSize(1_000)
    })
    .build() {
    fun findAllByClientId(clientId: ULID): Flow<ClientScopeData> {
        return findAll(where(ClientScopeData::clientId).`is`(clientId))
    }

    fun findAllByScopeTokenId(scopeTokenId: ULID): Flow<ClientScopeData> {
        return findAll(where(ClientScopeData::scopeTokenId).`is`(scopeTokenId))
    }

    suspend fun deleteAllByClientId(clientId: ULID) {
        deleteAll(where(ClientScopeData::clientId).`is`(clientId))
    }

    suspend fun deleteAllByScopeTokenId(scopeTokenId: ULID) {
        deleteAll(where(ClientScopeData::scopeTokenId).`is`(scopeTokenId))
    }
}
