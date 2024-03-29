package io.github.siyual_park.client.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.cache.CacheBuilder
import io.github.siyual_park.client.entity.ClientEntity
import io.github.siyual_park.data.cache.StorageManager
import io.github.siyual_park.data.repository.QueryableRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepositoryBuilder
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.ulid.ULID
import org.redisson.api.RedissonClient
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.Instant

@Repository
class ClientEntityRepository(
    entityOperations: R2dbcEntityOperations,
    objectMapper: ObjectMapper? = null,
    redisClient: RedissonClient? = null,
    eventPublisher: EventPublisher? = null,
    cacheStorageManager: StorageManager? = null
) : QueryableRepository<ClientEntity, ULID> by R2DBCRepositoryBuilder<ClientEntity, ULID>(entityOperations, ClientEntity::class)
    .enableEvent(eventPublisher)
    .enableJsonMapping(objectMapper)
    .enableCache(redisClient, expiredAt = { Instant.now().plus(Duration.ofHours(1)) }, size = 100_000)
    .enableCache({
        CacheBuilder.newBuilder()
            .softValues()
            .expireAfterWrite(Duration.ofMinutes(1))
            .maximumSize(1_000)
    })
    .enableCacheStorageManager(cacheStorageManager)
    .build()
