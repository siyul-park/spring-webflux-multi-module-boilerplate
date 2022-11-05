package io.github.siyual_park.auth.repository

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.auth.entity.TokenEntity
import io.github.siyual_park.data.cache.StorageManager
import io.github.siyual_park.data.repository.QueryableRepository
import io.github.siyual_park.data.repository.mongo.MongoRepositoryBuilder
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.ulid.ULID
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class TokenEntityRepository(
    template: ReactiveMongoTemplate,
    eventPublisher: EventPublisher? = null,
    cacheStorageManager: StorageManager? = null
) : QueryableRepository<TokenEntity, ULID> by MongoRepositoryBuilder<TokenEntity, ULID>(template, TokenEntity::class)
    .enableEvent(eventPublisher)
    .enableCache({
        CacheBuilder.newBuilder()
            .softValues()
            .expireAfterWrite(Duration.ofMinutes(1))
            .maximumSize(1_000)
    })
    .enableCacheStorageManager(cacheStorageManager)
    .build()
