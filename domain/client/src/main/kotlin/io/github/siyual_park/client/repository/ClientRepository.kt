package io.github.siyual_park.client.repository

import io.github.siyual_park.client.entity.ClientData
import io.github.siyual_park.data.repository.r2dbc.CachedR2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.SoftDeletedR2DBCRepository
import io.github.siyual_park.event.EventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository

@Repository
class ClientRepository(
    entityOperations: R2dbcEntityOperations,
    eventPublisher: EventPublisher? = null
) : R2DBCRepository<ClientData, Long> by CachedR2DBCRepository.of(
    SoftDeletedR2DBCRepository(
        entityOperations,
        ClientData::class,
        eventPublisher = eventPublisher
    )
)
