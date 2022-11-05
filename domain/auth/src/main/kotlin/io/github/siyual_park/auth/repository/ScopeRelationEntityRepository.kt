package io.github.siyual_park.auth.repository

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.auth.entity.ScopeRelationEntity
import io.github.siyual_park.data.cache.StorageManager
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.data.repository.QueryableRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepositoryBuilder
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class ScopeRelationEntityRepository(
    entityOperations: R2dbcEntityOperations,
    eventPublisher: EventPublisher? = null,
    cacheStorageManager: StorageManager? = null
) : QueryableRepository<ScopeRelationEntity, Long> by R2DBCRepositoryBuilder<ScopeRelationEntity, Long>(entityOperations, ScopeRelationEntity::class)
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

    fun findAllByChildId(childId: ULID): Flow<ScopeRelationEntity> {
        return findAll(where(ScopeRelationEntity::childId).`is`(childId))
    }

    fun findAllByParentId(parentIds: Iterable<ULID>): Flow<ScopeRelationEntity> {
        return findAll(where(ScopeRelationEntity::parentId).`in`(parentIds.toList()))
    }

    fun findAllByParentId(parentId: ULID): Flow<ScopeRelationEntity> {
        return findAll(where(ScopeRelationEntity::parentId).`is`(parentId))
    }

    suspend fun deleteAllByChildId(childId: ULID) {
        return deleteAll(where(ScopeRelationEntity::childId).`is`(childId))
    }

    suspend fun deleteAllByParentId(parentId: ULID) {
        return deleteAll(where(ScopeRelationEntity::parentId).`is`(parentId))
    }
}
