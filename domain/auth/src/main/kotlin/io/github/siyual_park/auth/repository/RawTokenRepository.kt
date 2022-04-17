package io.github.siyual_park.auth.repository

import io.github.siyual_park.auth.entity.TokenData
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.SimpleR2DBCRepository
import io.github.siyual_park.event.EventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository

@Repository
class RawTokenRepository(
    entityOperations: R2dbcEntityOperations,
    eventPublisher: EventPublisher? = null
) : R2DBCRepository<TokenData, Long> by SimpleR2DBCRepository(
    entityOperations,
    TokenData::class,
    eventPublisher = eventPublisher
)
