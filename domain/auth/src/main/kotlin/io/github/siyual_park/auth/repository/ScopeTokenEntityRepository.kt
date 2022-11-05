package io.github.siyual_park.auth.repository

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.auth.entity.ScopeTokenEntity
import io.github.siyual_park.data.cache.StorageManager
import io.github.siyual_park.data.repository.QueryableRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepositoryBuilder
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.ulid.ULID
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class ScopeTokenEntityRepository(
    entityOperations: R2dbcEntityOperations,
    eventPublisher: EventPublisher? = null,
    cacheStorageManager: StorageManager? = null
) : QueryableRepository<ScopeTokenEntity, ULID> by R2DBCRepositoryBuilder<ScopeTokenEntity, ULID>(entityOperations, ScopeTokenEntity::class)
    .enableEvent(eventPublisher)
    .enableCache({
        CacheBuilder.newBuilder()
            .softValues()
            .expireAfterWrite(Duration.ofMinutes(1))
            .maximumSize(1_000)
    })
    .enableCacheStorageManager(cacheStorageManager)
    .build()
