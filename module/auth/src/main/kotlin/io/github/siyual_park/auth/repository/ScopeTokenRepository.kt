package io.github.siyual_park.auth.repository

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.auth.entity.ScopeTokenData
import io.github.siyual_park.data.repository.r2dbc.CachedR2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.github.siyual_park.event.EventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class ScopeTokenRepository(
    entityOperations: R2dbcEntityOperations,
    eventPublisher: EventPublisher? = null
) : R2DBCRepository<ScopeTokenData, Long> by CachedR2DBCRepository.of(
    entityOperations,
    ScopeTokenData::class,
    CacheBuilder.newBuilder()
        .softValues()
        .expireAfterAccess(Duration.ofMinutes(10))
        .expireAfterWrite(Duration.ofMinutes(30))
        .maximumSize(1_000),
    eventPublisher = eventPublisher
)
