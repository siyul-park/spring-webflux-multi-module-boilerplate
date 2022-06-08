package io.github.siyual_park.auth.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.cache.CacheBuilder
import io.github.siyual_park.auth.entity.ScopeTokenData
import io.github.siyual_park.data.repository.QueryRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepositoryBuilder
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.ulid.ULID
import org.redisson.api.RedissonReactiveClient
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class ScopeTokenRepository(
    entityOperations: R2dbcEntityOperations,
    redisClient: RedissonReactiveClient? = null,
    objectMapper: ObjectMapper? = null,
    eventPublisher: EventPublisher? = null
) : QueryRepository<ScopeTokenData, ULID> by R2DBCRepositoryBuilder<ScopeTokenData, ULID>(entityOperations, ScopeTokenData::class)
    .enableEvent(eventPublisher)
    .enableJsonMapping(objectMapper)
    .enableCache({
        CacheBuilder.newBuilder()
            .softValues()
            .expireAfterAccess(Duration.ofMinutes(1))
            .expireAfterWrite(Duration.ofMinutes(2))
            .maximumSize(1_000)
    })
    .enableCache(
        redisClient = redisClient,
        ttl = Duration.ofHours(1),
        size = 5_000
    )
    .build()
