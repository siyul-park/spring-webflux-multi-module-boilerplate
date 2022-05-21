package io.github.siyual_park.client.repository

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.client.entity.ClientCredentialData
import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepositoryBuilder
import io.github.siyual_park.data.repository.r2dbc.where
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.ulid.ULID
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class ClientCredentialRepository(
    entityOperations: R2dbcEntityOperations,
    eventPublisher: EventPublisher? = null
) : R2DBCRepository<ClientCredentialData, Long> by R2DBCRepositoryBuilder<ClientCredentialData, Long>(entityOperations, ClientCredentialData::class)
    .enableEvent(eventPublisher)
    .enableCache(
        CacheBuilder.newBuilder()
            .softValues()
            .expireAfterAccess(Duration.ofMinutes(2))
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(1_000)
    )
    .enableQueryCache({
        CacheBuilder.newBuilder()
            .softValues()
            .expireAfterAccess(Duration.ofSeconds(1))
            .maximumSize(1_000)
    })
    .build() {
    suspend fun findByClientIdOrFail(clientId: ULID): ClientCredentialData {
        return findByClientId(clientId) ?: throw EmptyResultDataAccessException(1)
    }

    suspend fun findByClientId(clientId: ULID): ClientCredentialData? {
        return findOne(where(ClientCredentialData::clientId).`is`(clientId))
    }

    suspend fun existsByClientId(clientId: ULID): Boolean {
        return exists(where(ClientCredentialData::clientId).`is`(clientId))
    }

    suspend fun updateByClientId(clientId: ULID, patch: AsyncPatch<ClientCredentialData>): ClientCredentialData? {
        return update(where(ClientCredentialData::clientId).`is`(clientId), patch)
    }

    suspend fun deleteByClientId(clientId: ULID) {
        deleteAll(where(ClientCredentialData::clientId).`is`(clientId))
    }
}
