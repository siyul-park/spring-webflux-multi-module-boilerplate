package io.github.siyual_park.data.repository.r2dbc

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.repository.cache.CacheProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.CriteriaDefinition

class QueryCachedR2DBCRepository<T : Any, ID : Any>(
    private val delegator: R2DBCRepository<T, ID>,
    cacheBuilder: () -> CacheBuilder<Any, Any>,
) : R2DBCRepository<T, ID> {
    override val entityManager: EntityManager<T, ID>
        get() = delegator.entityManager

    private val singleCacheProvider = CacheProvider<String, T?>(cacheBuilder())
    private val multiCacheProvider = CacheProvider<SelectQuery, Collection<T>>(cacheBuilder())

    override suspend fun create(entity: T): T {
        clear()
        return delegator.create(entity)
    }

    override fun createAll(entities: Flow<T>): Flow<T> {
        return delegator.createAll(entities).onStart { clear() }
    }

    override fun createAll(entities: Iterable<T>): Flow<T> {
        return delegator.createAll(entities).onStart { clear() }
    }

    override suspend fun existsById(id: ID): Boolean {
        return delegator.existsById(id)
    }

    override suspend fun count(): Long {
        return delegator.count()
    }

    override suspend fun count(criteria: CriteriaDefinition?): Long {
        return delegator.count(criteria)
    }

    override suspend fun findById(id: ID): T? {
        return singleCacheProvider.get(where(entityManager.idProperty).`is`(id).toString()) {
            delegator.findById(id)
        }
    }

    override fun findAll(): Flow<T> {
        val query = SelectQuery(null, null, null, null)
        return flow {
            emitAll(
                multiCacheProvider.get(query) {
                    delegator.findAll().toList()
                }.asFlow()
            )
        }
    }

    override fun findAllById(ids: Iterable<ID>): Flow<T> {
        val query = SelectQuery(where(entityManager.idProperty).`in`(ids.toList()).toString(), ids.count(), null, null)
        return flow {
            emitAll(
                multiCacheProvider.get(query) {
                    delegator.findAllById(ids).toList()
                }.asFlow()
            )
        }
    }

    override suspend fun findOne(criteria: CriteriaDefinition): T? {
        return singleCacheProvider.get(criteria.toString()) {
            delegator.findOne(criteria)
        }
    }

    override fun findAll(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        val query = SelectQuery(criteria?.toString(), limit, offset, sort)
        return flow {
            emitAll(
                multiCacheProvider.get(query) {
                    delegator.findAll(criteria, limit, offset, sort).toList()
                }.asFlow()
            )
        }
    }

    override suspend fun updateById(id: ID, patch: Patch<T>): T? {
        clear()
        return delegator.updateById(id, patch)
    }

    override suspend fun updateById(id: ID, patch: AsyncPatch<T>): T? {
        clear()
        return delegator.updateById(id, patch)
    }

    override suspend fun update(entity: T): T? {
        clear()
        return delegator.update(entity)
    }

    override suspend fun update(entity: T, patch: Patch<T>): T? {
        clear()
        return delegator.update(entity, patch)
    }

    override suspend fun update(entity: T, patch: AsyncPatch<T>): T? {
        clear()
        return delegator.update(entity, patch)
    }

    override fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T?> {
        return delegator.updateAllById(ids, patch).onStart { clear() }
    }

    override fun updateAllById(ids: Iterable<ID>, patch: AsyncPatch<T>): Flow<T?> {
        return delegator.updateAllById(ids, patch).onStart { clear() }
    }

    override fun updateAll(entity: Iterable<T>): Flow<T?> {
        return delegator.updateAll(entity).onStart { clear() }
    }

    override fun updateAll(entity: Iterable<T>, patch: Patch<T>): Flow<T?> {
        return delegator.updateAll(entity, patch).onStart { clear() }
    }

    override fun updateAll(entity: Iterable<T>, patch: AsyncPatch<T>): Flow<T?> {
        return delegator.updateAll(entity, patch).onStart { clear() }
    }

    override suspend fun update(criteria: CriteriaDefinition, patch: Patch<T>): T? {
        clear()
        return delegator.update(criteria, patch)
    }

    override suspend fun update(criteria: CriteriaDefinition, patch: AsyncPatch<T>): T? {
        clear()
        return delegator.update(criteria, patch)
    }

    override fun updateAll(
        criteria: CriteriaDefinition,
        patch: Patch<T>,
        limit: Int?,
        offset: Long?,
        sort: Sort?
    ): Flow<T> {
        return delegator.updateAll(criteria, patch, limit, offset, sort).onStart { clear() }
    }

    override fun updateAll(
        criteria: CriteriaDefinition,
        patch: AsyncPatch<T>,
        limit: Int?,
        offset: Long?,
        sort: Sort?
    ): Flow<T> {
        return delegator.updateAll(criteria, patch, limit, offset, sort).onStart { clear() }
    }

    override suspend fun deleteById(id: ID) {
        clear()
        return delegator.deleteById(id)
    }

    override suspend fun delete(entity: T) {
        clear()
        return delegator.delete(entity)
    }

    override suspend fun deleteAllById(ids: Iterable<ID>) {
        clear()
        return delegator.deleteAllById(ids)
    }

    override suspend fun deleteAll(entities: Iterable<T>) {
        clear()
        return delegator.deleteAll(entities)
    }

    override suspend fun deleteAll() {
        clear()
        return delegator.deleteAll()
    }

    override suspend fun exists(criteria: CriteriaDefinition): Boolean {
        return delegator.exists(criteria)
    }

    override suspend fun deleteAll(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?) {
        clear()
        return delegator.deleteAll(criteria, limit, offset, sort)
    }

    private suspend fun clear() {
        singleCacheProvider.clear()
        multiCacheProvider.clear()
    }
}
