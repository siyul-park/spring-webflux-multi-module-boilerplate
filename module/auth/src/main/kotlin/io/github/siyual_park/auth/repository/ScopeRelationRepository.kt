package io.github.siyual_park.auth.repository

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.auth.entity.ScopeRelation
import io.github.siyual_park.auth.entity.ScopeToken
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
) : R2DBCRepository<ScopeRelation, Long> by CachedR2DBCRepository.of(
    entityOperations,
    ScopeRelation::class,
    {
        CacheBuilder.newBuilder()
            .softValues()
            .expireAfterAccess(Duration.ofMinutes(10))
            .expireAfterWrite(Duration.ofMinutes(30))
            .maximumSize(1_000)
    }
) {
    suspend fun findByOrFail(parent: ScopeToken, child: ScopeToken): ScopeRelation {
        return findBy(parent, child) ?: throw EmptyResultDataAccessException(1)
    }

    suspend fun findBy(parent: ScopeToken, child: ScopeToken): ScopeRelation? {
        if (parent.id == null || child.id == null) {
            return null
        }
        return findBy(parent.id!!, child.id!!)
    }

    suspend fun findByOrFail(parentId: Long, childId: Long): ScopeRelation {
        return findBy(parentId, childId) ?: throw EmptyResultDataAccessException(1)
    }

    suspend fun findBy(parentId: Long, childId: Long): ScopeRelation? {
        return findOne(
            where(ScopeRelation::parentId).`is`(parentId)
                .and(where(ScopeRelation::childId).`is`(childId))
        )
    }

    fun findAllByChild(child: ScopeToken): Flow<ScopeRelation> {
        return child.id?.let { findAllByChildId(it) } ?: emptyFlow()
    }

    fun findAllByChildId(childId: Long): Flow<ScopeRelation> {
        return findAll(where(ScopeRelation::childId).`is`(childId))
    }

    fun findAllByParent(parent: ScopeToken): Flow<ScopeRelation> {
        return parent.id?.let { findAllByParentId(it) } ?: emptyFlow()
    }

    fun findAllByParentId(parentId: Long): Flow<ScopeRelation> {
        return findAll(where(ScopeRelation::parentId).`is`(parentId))
    }
}
