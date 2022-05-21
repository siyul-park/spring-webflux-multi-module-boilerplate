package io.github.siyual_park.user.repository

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepositoryBuilder
import io.github.siyual_park.data.repository.r2dbc.where
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.ulid.ULID
import io.github.siyual_park.user.entity.UserScopeData
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class UserScopeRepository(
    entityOperations: R2dbcEntityOperations,
    eventPublisher: EventPublisher? = null
) : R2DBCRepository<UserScopeData, Long> by R2DBCRepositoryBuilder<UserScopeData, Long>(entityOperations, UserScopeData::class)
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
    fun findAllByUserId(userId: ULID): Flow<UserScopeData> {
        return findAll(where(UserScopeData::userId).`is`(userId))
    }

    fun findAllByScopeTokenId(scopeTokenId: ULID): Flow<UserScopeData> {
        return findAll(where(UserScopeData::scopeTokenId).`is`(scopeTokenId))
    }

    suspend fun deleteAllByUserId(userId: ULID) {
        deleteAll(where(UserScopeData::userId).`is`(userId))
    }

    suspend fun deleteAllByScopeTokenId(scopeTokenId: ULID) {
        deleteAll(where(UserScopeData::scopeTokenId).`is`(scopeTokenId))
    }
}
