package io.github.siyual_park.client.repository

import io.github.siyual_park.client.entity.Client
import io.github.siyual_park.client.entity.ClientCredential
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.repository.r2dbc.CachedR2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository

@Repository
class ClientCredentialRepository(
    entityOperations: R2dbcEntityOperations
) : R2DBCRepository<ClientCredential, Long> by CachedR2DBCRepository.of(
    entityOperations,
    ClientCredential::class
) {
    suspend fun findByClientOrFail(client: Client): ClientCredential {
        return findByClient(client) ?: throw EmptyResultDataAccessException(1)
    }

    suspend fun findByClient(client: Client): ClientCredential? {
        return client.id?.let { findByClientId(it) }
    }

    suspend fun findByClientIdOrFail(clientId: Long): ClientCredential {
        return findByClientId(clientId) ?: throw EmptyResultDataAccessException(1)
    }

    suspend fun findByClientId(clientId: Long): ClientCredential? {
        return findOne(where(ClientCredential::clientId).`is`(clientId))
    }

    suspend fun existsByClient(client: Client): Boolean {
        return client.id?.let { existsByClientId(it) } ?: false
    }

    suspend fun existsByClientId(clientId: Long): Boolean {
        return exists(where(ClientCredential::clientId).`is`(clientId))
    }

    suspend fun updateByClient(client: Client, patch: AsyncPatch<ClientCredential>): ClientCredential? {
        return client.id?.let { updateByClientId(it, patch) }
    }

    suspend fun updateByClientId(clientId: Long, patch: AsyncPatch<ClientCredential>): ClientCredential? {
        return update(where(ClientCredential::clientId).`is`(clientId), patch)
    }

    suspend fun deleteByClient(client: Client) {
        client.id?.let { deleteByClientId(it) }
    }

    suspend fun deleteByClientId(clientId: Long) {
        deleteAll(where(ClientCredential::clientId).`is`(clientId))
    }
}
