package io.github.siyual_park.auth.repository

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.auth.entity.TokenData
import io.github.siyual_park.data.repository.mongo.CachedMongoRepository
import io.github.siyual_park.data.repository.mongo.MongoRepository
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.ulid.ULID
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class TokenRepository(
    template: ReactiveMongoTemplate,
    eventPublisher: EventPublisher? = null
) : MongoRepository<TokenData, ULID> by CachedMongoRepository.of(
    template,
    TokenData::class,
    CacheBuilder.newBuilder()
        .softValues()
        .expireAfterAccess(Duration.ofMinutes(1))
        .expireAfterWrite(Duration.ofMinutes(2))
        .maximumSize(100_000),
    eventPublisher = eventPublisher
)
