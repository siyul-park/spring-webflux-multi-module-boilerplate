package io.github.siyual_park.client.repository

import io.github.siyual_park.client.entity.Client
import io.github.siyual_park.data.repository.r2dbc.CachedR2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository

@Repository
class ClientRepository(
    entityOperations: R2dbcEntityOperations
) : R2DBCRepository<Client, Long> by CachedR2DBCRepository.of(
    entityOperations,
    Client::class
)
