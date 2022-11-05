package io.github.siyual_park.client.repository

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.client.entity.ClientScopeEntity
import io.github.siyual_park.data.cache.StorageManager
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.data.repository.QueryableRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepositoryBuilder
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class ClientScopeEntityRepository(
    entityOperations: R2dbcEntityOperations,
    eventPublisher: EventPublisher? = null,
    cacheStorageManager: StorageManager? = null
) : QueryableRepository<ClientScopeEntity, Long> by R2DBCRepositoryBuilder<ClientScopeEntity, Long>(entityOperations, ClientScopeEntity::class)
    .enableEvent(eventPublisher)
    .enableCache({
        CacheBuilder.newBuilder()
            .softValues()
            .expireAfterWrite(Duration.ofMinutes(1))
            .maximumSize(1_000)
    })
    .enableQueryCache({
        CacheBuilder.newBuilder()
            .softValues()
            .expireAfterWrite(Duration.ofSeconds(1))
            .maximumSize(1_000)
    })
    .enableCacheStorageManager(cacheStorageManager)
    .build() {
    fun findAllByClientId(clientId: ULID): Flow<ClientScopeEntity> {
        return findAll(where(ClientScopeEntity::clientId).`is`(clientId))
    }

    fun findAllByScopeTokenId(scopeTokenId: ULID): Flow<ClientScopeEntity> {
        return findAll(where(ClientScopeEntity::scopeTokenId).`is`(scopeTokenId))
    }

    suspend fun deleteAllByClientId(clientId: ULID) {
        deleteAll(where(ClientScopeEntity::clientId).`is`(clientId))
    }

    suspend fun deleteAllByScopeTokenId(scopeTokenId: ULID) {
        deleteAll(where(ClientScopeEntity::scopeTokenId).`is`(scopeTokenId))
    }
}
