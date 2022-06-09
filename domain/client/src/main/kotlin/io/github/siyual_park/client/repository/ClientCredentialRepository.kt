package io.github.siyual_park.client.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.cache.CacheBuilder
import io.github.siyual_park.client.entity.ClientCredentialData
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.data.patch.SuspendPatch
import io.github.siyual_park.data.repository.QueryRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepositoryBuilder
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.ulid.ULID
import org.redisson.api.RedissonReactiveClient
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class ClientCredentialRepository(
    entityOperations: R2dbcEntityOperations,
    objectMapper: ObjectMapper? = null,
    redisClient: RedissonReactiveClient,
    eventPublisher: EventPublisher? = null
) : QueryRepository<ClientCredentialData, Long> by R2DBCRepositoryBuilder<ClientCredentialData, Long>(entityOperations, ClientCredentialData::class)
    .enableEvent(eventPublisher)
    .enableJsonMapping(objectMapper)
    .enableCache({
        CacheBuilder.newBuilder()
            .softValues()
            .expireAfterWrite(Duration.ofSeconds(1))
            .maximumSize(1_000)
    })
    .enableCache(redisClient, ttl = Duration.ofHours(1), size = 10_000)
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

    suspend fun updateByClientId(clientId: ULID, patch: SuspendPatch<ClientCredentialData>): ClientCredentialData? {
        return update(where(ClientCredentialData::clientId).`is`(clientId), patch)
    }

    suspend fun deleteByClientId(clientId: ULID) {
        deleteAll(where(ClientCredentialData::clientId).`is`(clientId))
    }
}
