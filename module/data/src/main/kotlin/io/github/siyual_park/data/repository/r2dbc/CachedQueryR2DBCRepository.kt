package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.cache.QueryStorage
import io.github.siyual_park.data.cache.SelectQuery
import io.github.siyual_park.data.cache.get
import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.CriteriaDefinition

class CachedQueryR2DBCRepository<T : Any, ID : Any>(
    private val delegator: R2DBCRepository<T, ID>,
    private val storage: QueryStorage<T>
) : R2DBCRepository<T, ID> {
    override val entityManager: EntityManager<T, ID>
        get() = delegator.entityManager

    override suspend fun create(entity: T): T {
        storage.clear()
        return delegator.create(entity)
    }

    override fun createAll(entities: Flow<T>): Flow<T> {
        return flow {
            storage.clear()
            emitAll(delegator.createAll(entities))
        }
    }

    override fun createAll(entities: Iterable<T>): Flow<T> {
        return flow {
            storage.clear()
            emitAll(delegator.createAll(entities))
        }
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
        return storage.getIfPresent(where(entityManager.idProperty).`is`(id).toString()) {
            delegator.findById(id)
        }
    }

    override fun findAll(): Flow<T> {
        return storage.get(SelectQuery(null, null, null, null)) {
            delegator.findAll()
        }
    }

    override fun findAllById(ids: Iterable<ID>): Flow<T> {
        val query = SelectQuery(where(entityManager.idProperty).`in`(ids.toList()).toString(), ids.count(), null, null)
        return storage.get(query) {
            delegator.findAllById(ids)
        }
    }

    override suspend fun findOne(criteria: CriteriaDefinition): T? {
        return storage.getIfPresent(criteria.toString()) {
            delegator.findOne(criteria)
        }
    }

    override fun findAll(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        val query = SelectQuery(criteria?.toString(), limit, offset, sort)
        return storage.get(query) {
            delegator.findAll(criteria, limit, offset, sort)
        }
    }

    override suspend fun updateById(id: ID, patch: Patch<T>): T? {
        storage.clear()
        return delegator.updateById(id, patch)
    }

    override suspend fun updateById(id: ID, patch: AsyncPatch<T>): T? {
        storage.clear()
        return delegator.updateById(id, patch)
    }

    override suspend fun update(entity: T): T? {
        storage.clear()
        return delegator.update(entity)
    }

    override suspend fun update(entity: T, patch: Patch<T>): T? {
        storage.clear()
        return delegator.update(entity, patch)
    }

    override suspend fun update(entity: T, patch: AsyncPatch<T>): T? {
        storage.clear()
        return delegator.update(entity, patch)
    }

    override fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T?> {
        return flow {
            storage.clear()
            emitAll(delegator.updateAllById(ids, patch))
        }
    }

    override fun updateAllById(ids: Iterable<ID>, patch: AsyncPatch<T>): Flow<T?> {
        return flow {
            storage.clear()
            emitAll(delegator.updateAllById(ids, patch))
        }
    }

    override fun updateAll(entity: Iterable<T>): Flow<T?> {
        return flow {
            storage.clear()
            emitAll(delegator.updateAll(entity))
        }
    }

    override fun updateAll(entity: Iterable<T>, patch: Patch<T>): Flow<T?> {
        return flow {
            storage.clear()
            emitAll(delegator.updateAll(entity, patch))
        }
    }

    override fun updateAll(entity: Iterable<T>, patch: AsyncPatch<T>): Flow<T?> {
        return flow {
            storage.clear()
            emitAll(delegator.updateAll(entity, patch))
        }
    }

    override suspend fun update(criteria: CriteriaDefinition, patch: Patch<T>): T? {
        storage.clear()
        return delegator.update(criteria, patch)
    }

    override suspend fun update(criteria: CriteriaDefinition, patch: AsyncPatch<T>): T? {
        storage.clear()
        return delegator.update(criteria, patch)
    }

    override fun updateAll(
        criteria: CriteriaDefinition,
        patch: Patch<T>,
        limit: Int?,
        offset: Long?,
        sort: Sort?
    ): Flow<T> {
        return flow {
            storage.clear()
            emitAll(delegator.updateAll(criteria, patch, limit, offset, sort))
        }
    }

    override fun updateAll(
        criteria: CriteriaDefinition,
        patch: AsyncPatch<T>,
        limit: Int?,
        offset: Long?,
        sort: Sort?
    ): Flow<T> {
        return flow {
            storage.clear()
            emitAll(delegator.updateAll(criteria, patch, limit, offset, sort))
        }
    }

    override suspend fun deleteById(id: ID) {
        storage.clear()
        return delegator.deleteById(id)
    }

    override suspend fun delete(entity: T) {
        storage.clear()
        return delegator.delete(entity)
    }

    override suspend fun deleteAllById(ids: Iterable<ID>) {
        storage.clear()
        return delegator.deleteAllById(ids)
    }

    override suspend fun deleteAll(entities: Iterable<T>) {
        storage.clear()
        return delegator.deleteAll(entities)
    }

    override suspend fun deleteAll() {
        storage.clear()
        return delegator.deleteAll()
    }

    override suspend fun exists(criteria: CriteriaDefinition): Boolean {
        return delegator.exists(criteria)
    }

    override suspend fun deleteAll(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?) {
        storage.clear()
        return delegator.deleteAll(criteria, limit, offset, sort)
    }
}
