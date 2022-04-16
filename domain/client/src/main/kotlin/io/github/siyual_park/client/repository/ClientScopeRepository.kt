package io.github.siyual_park.client.repository

import io.github.siyual_park.client.entity.ClientScopeData
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.repository.r2dbc.CachedR2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository

@Repository
class ClientScopeRepository(
    entityOperations: R2dbcEntityOperations,
    eventPublisher: EventPublisher? = null
) : R2DBCRepository<ClientScopeData, Long> by CachedR2DBCRepository.of(
    entityOperations,
    ClientScopeData::class,
    eventPublisher = eventPublisher
) {
    fun findAllByClientId(clientId: ULID): Flow<ClientScopeData> {
        return findAll(where(ClientScopeData::clientId).`is`(clientId))
    }

    fun findAllByScopeTokenId(scopeTokenId: ULID): Flow<ClientScopeData> {
        return findAll(where(ClientScopeData::scopeTokenId).`is`(scopeTokenId))
    }

    suspend fun deleteAllByClientId(clientId: ULID) {
        deleteAll(where(ClientScopeData::clientId).`is`(clientId))
    }

    suspend fun deleteAllByScopeTokenId(scopeTokenId: ULID) {
        deleteAll(where(ClientScopeData::scopeTokenId).`is`(scopeTokenId))
    }
}
