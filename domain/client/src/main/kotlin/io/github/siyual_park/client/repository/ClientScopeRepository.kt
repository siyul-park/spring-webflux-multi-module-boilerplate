package io.github.siyual_park.client.repository

import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.client.entity.Client
import io.github.siyual_park.client.entity.ClientScope
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
) : R2DBCRepository<ClientScope, Long> by CachedR2DBCRepository.of(
    entityOperations,
    ClientScope::class
) {
    fun findAllByClient(client: Client): Flow<ClientScope> {
        return client.id?.let { findAllByClientId(it) } ?: emptyFlow()
    }

    fun findAllByClientId(clientId: Long): Flow<ClientScope> {
        return findAll(where(ClientScope::clientId).`is`(clientId))
    }

    fun findAllByScopeToken(scopeToken: ScopeToken): Flow<ClientScope> {
        return scopeToken.id?.let { findAllByScopeTokenId(it) } ?: emptyFlow()
    }

    fun findAllByScopeTokenId(scopeTokenId: Long): Flow<ClientScope> {
        return findAll(where(ClientScope::scopeTokenId).`is`(scopeTokenId))
    }

    suspend fun deleteAllByClient(client: Client) {
        client.id?.let { deleteAllByClientId(it) }
    }

    suspend fun deleteAllByClientId(clientId: Long) {
        deleteAll(where(ClientScope::clientId).`is`(clientId))
    }
}
