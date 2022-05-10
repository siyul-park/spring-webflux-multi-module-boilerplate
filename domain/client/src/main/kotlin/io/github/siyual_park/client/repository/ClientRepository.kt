package io.github.siyual_park.client.repository

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.client.entity.ClientData
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepositoryBuilder
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.ulid.ULID
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class ClientRepository(
    entityOperations: R2dbcEntityOperations,
    eventPublisher: EventPublisher? = null
) : R2DBCRepository<ClientData, ULID> by R2DBCRepositoryBuilder<ClientData, ULID>(entityOperations, ClientData::class)
    .set(eventPublisher)
    .set(
        CacheBuilder.newBuilder()
            .softValues()
            .expireAfterAccess(Duration.ofMinutes(2))
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(1_000)
    )
    .build()
