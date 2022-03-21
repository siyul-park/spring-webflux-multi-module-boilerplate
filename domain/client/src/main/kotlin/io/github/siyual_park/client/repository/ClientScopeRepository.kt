package io.github.siyual_park.client.repository

import io.github.siyual_park.auth.entity.ScopeTokenData
import io.github.siyual_park.client.entity.ClientData
import io.github.siyual_park.client.entity.ClientScopeData
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.repository.r2dbc.CachedR2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository

@Repository
class ClientScopeRepository(
    entityOperations: R2dbcEntityOperations
) : R2DBCRepository<ClientScopeData, Long> by CachedR2DBCRepository.of(
    entityOperations,
    ClientScopeData::class
) {
    fun findAllByClient(client: ClientData): Flow<ClientScopeData> {
        return client.id?.let { findAllByClientId(it) } ?: emptyFlow()
    }

    fun findAllByClientId(clientId: Long): Flow<ClientScopeData> {
        return findAll(where(ClientScopeData::clientId).`is`(clientId))
    }

    fun findAllByScopeToken(scopeToken: ScopeTokenData): Flow<ClientScopeData> {
        return scopeToken.id?.let { findAllByScopeTokenId(it) } ?: emptyFlow()
    }

    fun findAllByScopeTokenId(scopeTokenId: Long): Flow<ClientScopeData> {
        return findAll(where(ClientScopeData::scopeTokenId).`is`(scopeTokenId))
    }

    suspend fun deleteAllByClient(client: ClientData) {
        client.id?.let { deleteAllByClientId(it) }
    }

    suspend fun deleteAllByClientId(clientId: Long) {
        deleteAll(where(ClientScopeData::clientId).`is`(clientId))
    }
}
