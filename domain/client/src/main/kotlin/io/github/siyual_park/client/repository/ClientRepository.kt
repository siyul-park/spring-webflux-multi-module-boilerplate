package io.github.siyual_park.client.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.cache.CacheBuilder
import io.github.siyual_park.client.entity.ClientData
import io.github.siyual_park.data.repository.QueryRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepositoryBuilder
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.ulid.ULID
import org.redisson.api.RedissonReactiveClient
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class ClientRepository(
    entityOperations: R2dbcEntityOperations,
    objectMapper: ObjectMapper? = null,
    redisClient: RedissonReactiveClient,
    eventPublisher: EventPublisher? = null
) : QueryRepository<ClientData, ULID> by R2DBCRepositoryBuilder<ClientData, ULID>(entityOperations, ClientData::class)
    .enableEvent(eventPublisher)
    .enableJsonMapping(objectMapper)
    .enableCache({
        CacheBuilder.newBuilder()
            .softValues()
            .expireAfterWrite(Duration.ofSeconds(1))
            .maximumSize(1_000)
    })
    .enableCache(redisClient, ttl = Duration.ofHours(1), size = 10_000)
    .build()
