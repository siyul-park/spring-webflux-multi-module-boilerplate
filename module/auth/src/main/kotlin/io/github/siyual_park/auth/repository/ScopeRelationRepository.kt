package io.github.siyual_park.auth.repository

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.auth.entity.ScopeRelationData
import io.github.siyual_park.auth.entity.ScopeTokenData
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.repository.r2dbc.CachedR2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class ScopeRelationRepository(
    entityOperations: R2dbcEntityOperations
) : R2DBCRepository<ScopeRelationData, Long> by CachedR2DBCRepository.of(
    entityOperations,
    ScopeRelationData::class,
    CacheBuilder.newBuilder()
        .softValues()
        .expireAfterAccess(Duration.ofMinutes(10))
        .expireAfterWrite(Duration.ofMinutes(30))
        .maximumSize(1_000)
) {
    suspend fun findByOrFail(parent: ScopeTokenData, child: ScopeTokenData): ScopeRelationData {
        return findBy(parent, child) ?: throw EmptyResultDataAccessException(1)
    }

    suspend fun findBy(parent: ScopeTokenData, child: ScopeTokenData): ScopeRelationData? {
        if (parent.id == null || child.id == null) {
            return null
        }
        return findBy(parent.id!!, child.id!!)
    }

    suspend fun findByOrFail(parentId: Long, childId: Long): ScopeRelationData {
        return findBy(parentId, childId) ?: throw EmptyResultDataAccessException(1)
    }

    suspend fun findBy(parentId: Long, childId: Long): ScopeRelationData? {
        return findOne(
            where(ScopeRelationData::parentId).`is`(parentId)
                .and(where(ScopeRelationData::childId).`is`(childId))
        )
    }

    fun findAllByChild(child: ScopeTokenData): Flow<ScopeRelationData> {
        return child.id?.let { findAllByChildId(it) } ?: emptyFlow()
    }

    fun findAllByChildId(childId: Long): Flow<ScopeRelationData> {
        return findAll(where(ScopeRelationData::childId).`is`(childId))
    }

    fun findAllByParent(parent: ScopeTokenData): Flow<ScopeRelationData> {
        return parent.id?.let { findAllByParentId(it) } ?: emptyFlow()
    }

    fun findAllByParentId(parentId: Long): Flow<ScopeRelationData> {
        return findAll(where(ScopeRelationData::parentId).`is`(parentId))
    }
}
