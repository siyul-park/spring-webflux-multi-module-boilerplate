package io.github.siyual_park.data.repository.cache

import io.github.siyual_park.data.cache.QueryStorage
import io.github.siyual_park.data.cache.SelectQuery
import io.github.siyual_park.data.cache.get
import io.github.siyual_park.data.cache.getIfPresent
import io.github.siyual_park.data.criteria.Criteria
import io.github.siyual_park.data.criteria.`in`
import io.github.siyual_park.data.criteria.`is`
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.data.expansion.idProperty
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.patch.SuspendPatch
import io.github.siyual_park.data.repository.QueryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import org.springframework.data.domain.Sort
import kotlin.reflect.KClass

class QueryCachedRepository<T : Any, ID : Any>(
    private val delegator: QueryRepository<T, ID>,
    private val storage: QueryStorage<T>,
    clazz: KClass<T>
) : QueryRepository<T, ID> {
    @Suppress("UNCHECKED_CAST")
    private val idProperty = idProperty<T, ID>(clazz)

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

    override suspend fun count(criteria: Criteria?, limit: Int?): Long {
        if (limit != null && limit <= 0) {
            return 0
        }
        return delegator.count(criteria, limit)
    }

    override suspend fun findById(id: ID): T? {
        return storage.getIfPresent(where(idProperty).`is`(id).toString()) {
            delegator.findById(id)
        }
    }

    override fun findAll(): Flow<T> {
        return storage.get(SelectQuery(null, null, null, null)) {
            delegator.findAll()
        }
    }

    override fun findAllById(ids: Iterable<ID>): Flow<T> {
        if (ids.count() == 0) {
            return emptyFlow()
        }
        val query = SelectQuery(where(idProperty).`in`(ids.toList()).toString(), ids.count(), null, null)
        return storage.get(query) {
            delegator.findAllById(ids)
        }
    }

    override suspend fun findOne(criteria: Criteria): T? {
        return storage.getIfPresent(criteria.toString()) {
            delegator.findOne(criteria)
        }
    }

    override fun findAll(criteria: Criteria?, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        if (limit != null && limit <= 0) {
            return emptyFlow()
        }
        val query = SelectQuery(criteria?.toString(), limit, offset, sort)
        return storage.get(query) {
            delegator.findAll(criteria, limit, offset, sort)
        }
    }

    override suspend fun updateById(id: ID, patch: Patch<T>): T? {
        storage.clear()
        return delegator.updateById(id, patch)
    }

    override suspend fun updateById(id: ID, patch: SuspendPatch<T>): T? {
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

    override suspend fun update(entity: T, patch: SuspendPatch<T>): T? {
        storage.clear()
        return delegator.update(entity, patch)
    }

    override fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T?> {
        if (ids.count() == 0) {
            return emptyFlow()
        }
        return flow {
            storage.clear()
            emitAll(delegator.updateAllById(ids, patch))
        }
    }

    override fun updateAllById(ids: Iterable<ID>, patch: SuspendPatch<T>): Flow<T?> {
        if (ids.count() == 0) {
            return emptyFlow()
        }
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

    override fun updateAll(entity: Iterable<T>, patch: SuspendPatch<T>): Flow<T?> {
        return flow {
            storage.clear()
            emitAll(delegator.updateAll(entity, patch))
        }
    }

    override suspend fun update(criteria: Criteria, patch: Patch<T>): T? {
        storage.clear()
        return delegator.update(criteria, patch)
    }

    override suspend fun update(criteria: Criteria, patch: SuspendPatch<T>): T? {
        storage.clear()
        return delegator.update(criteria, patch)
    }

    override fun updateAll(
        criteria: Criteria,
        patch: Patch<T>,
        limit: Int?,
        offset: Long?,
        sort: Sort?
    ): Flow<T> {
        if (limit != null && limit <= 0) {
            return emptyFlow()
        }
        return flow {
            storage.clear()
            emitAll(delegator.updateAll(criteria, patch, limit, offset, sort))
        }
    }

    override fun updateAll(
        criteria: Criteria,
        patch: SuspendPatch<T>,
        limit: Int?,
        offset: Long?,
        sort: Sort?
    ): Flow<T> {
        if (limit != null && limit <= 0) {
            return emptyFlow()
        }
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
        if (ids.count() == 0) {
            return
        }
        storage.clear()
        return delegator.deleteAllById(ids)
    }

    override suspend fun deleteAll(entities: Iterable<T>) {
        if (entities.count() == 0) {
            return
        }
        storage.clear()
        return delegator.deleteAll(entities)
    }

    override suspend fun deleteAll() {
        storage.clear()
        return delegator.deleteAll()
    }

    override suspend fun exists(criteria: Criteria): Boolean {
        return delegator.exists(criteria)
    }

    override suspend fun deleteAll(criteria: Criteria?, limit: Int?, offset: Long?, sort: Sort?) {
        if (limit != null && limit <= 0) {
            return
        }
        storage.clear()
        return delegator.deleteAll(criteria, limit, offset, sort)
    }
}
