package io.github.siyual_park.auth.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.cache.CacheBuilder
import io.github.siyual_park.auth.entity.TokenData
import io.github.siyual_park.data.repository.QueryRepository
import io.github.siyual_park.data.repository.mongo.MongoRepositoryBuilder
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.ulid.ULID
import org.redisson.api.RedissonReactiveClient
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class TokenRepository(
    template: ReactiveMongoTemplate,
    redisClient: RedissonReactiveClient? = null,
    objectMapper: ObjectMapper? = null,
    eventPublisher: EventPublisher? = null
) : QueryRepository<TokenData, ULID> by MongoRepositoryBuilder<TokenData, ULID>(template, TokenData::class)
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
        ttl = Duration.ofMinutes(1),
        size = 10_000
    )
    .build()
