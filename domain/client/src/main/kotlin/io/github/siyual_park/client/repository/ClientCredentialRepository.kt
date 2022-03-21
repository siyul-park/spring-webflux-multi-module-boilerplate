package io.github.siyual_park.client.repository

import io.github.siyual_park.client.entity.ClientCredentialData
import io.github.siyual_park.client.entity.ClientData
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
) : R2DBCRepository<ClientCredentialData, Long> by CachedR2DBCRepository.of(
    entityOperations,
    ClientCredentialData::class
) {
    suspend fun findByClientOrFail(client: ClientData): ClientCredentialData {
        return findByClient(client) ?: throw EmptyResultDataAccessException(1)
    }

    suspend fun findByClient(client: ClientData): ClientCredentialData? {
        return client.id?.let { findByClientId(it) }
    }

    suspend fun findByClientIdOrFail(clientId: Long): ClientCredentialData {
        return findByClientId(clientId) ?: throw EmptyResultDataAccessException(1)
    }

    suspend fun findByClientId(clientId: Long): ClientCredentialData? {
        return findOne(where(ClientCredentialData::clientId).`is`(clientId))
    }

    suspend fun existsByClient(client: ClientData): Boolean {
        return client.id?.let { existsByClientId(it) } ?: false
    }

    suspend fun existsByClientId(clientId: Long): Boolean {
        return exists(where(ClientCredentialData::clientId).`is`(clientId))
    }

    suspend fun updateByClient(client: ClientData, patch: AsyncPatch<ClientCredentialData>): ClientCredentialData? {
        return client.id?.let { updateByClientId(it, patch) }
    }

    suspend fun updateByClientId(clientId: Long, patch: AsyncPatch<ClientCredentialData>): ClientCredentialData? {
        return update(where(ClientCredentialData::clientId).`is`(clientId), patch)
    }

    suspend fun deleteByClient(client: ClientData) {
        client.id?.let { deleteByClientId(it) }
    }

    suspend fun deleteByClientId(clientId: Long) {
        deleteAll(where(ClientCredentialData::clientId).`is`(clientId))
    }
}
