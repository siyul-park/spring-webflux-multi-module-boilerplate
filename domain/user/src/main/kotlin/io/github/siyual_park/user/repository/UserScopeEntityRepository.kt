package io.github.siyual_park.user.repository

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.cache.StorageManager
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.data.repository.QueryableRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepositoryBuilder
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.ulid.ULID
import io.github.siyual_park.user.entity.UserScopeEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class UserScopeEntityRepository(
    entityOperations: R2dbcEntityOperations,
    eventPublisher: EventPublisher? = null,
    cacheStorageManager: StorageManager? = null
) : QueryableRepository<UserScopeEntity, Long> by R2DBCRepositoryBuilder<UserScopeEntity, Long>(entityOperations, UserScopeEntity::class)
    .enableEvent(eventPublisher)
    .enableCache({
        CacheBuilder.newBuilder()
            .softValues()
            .expireAfterWrite(Duration.ofMinutes(1))
            .maximumSize(1_000)
    })
    .enableQueryCache({
        CacheBuilder.newBuilder()
            .softValues()
            .expireAfterWrite(Duration.ofSeconds(1))
            .maximumSize(1_000)
    })
    .enableCacheStorageManager(cacheStorageManager)
    .build() {
    fun findAllByUserId(userId: ULID): Flow<UserScopeEntity> {
        return findAll(where(UserScopeEntity::userId).`is`(userId))
    }

    fun findAllByScopeTokenId(scopeTokenId: ULID): Flow<UserScopeEntity> {
        return findAll(where(UserScopeEntity::scopeTokenId).`is`(scopeTokenId))
    }

    suspend fun deleteAllByUserId(userId: ULID) {
        deleteAll(where(UserScopeEntity::userId).`is`(userId))
    }

    suspend fun deleteAllByScopeTokenId(scopeTokenId: ULID) {
        deleteAll(where(UserScopeEntity::scopeTokenId).`is`(scopeTokenId))
    }
}
