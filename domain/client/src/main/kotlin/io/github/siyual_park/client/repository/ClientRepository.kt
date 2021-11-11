package io.github.siyual_park.client.repository

import io.github.siyual_park.client.entity.Client
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.repository.r2dbc.CachedR2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository

@Repository
class ClientRepository(
    entityOperations: R2dbcEntityOperations
) : R2DBCRepository<Client, Long> by CachedR2DBCRepository.of(
    entityOperations,
    Client::class
) {
    suspend fun findByNameOrFail(name: String): Client {
        return findByName(name) ?: throw EmptyResultDataAccessException(1)
    }

    suspend fun findByName(name: String): Client? {
        return findOne(where(Client::name).`is`(name))
    }

    suspend fun existsByName(name: String): Boolean {
        return exists(where(Client::name).`is`(name))
    }
}
