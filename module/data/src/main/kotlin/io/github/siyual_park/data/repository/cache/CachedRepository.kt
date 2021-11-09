package io.github.siyual_park.data.repository.cache

import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.repository.Repository
import kotlinx.coroutines.flow.Flow
import org.apache.commons.collections4.map.ReferenceMap
import java.util.Collections.synchronizedMap

open class CachedRepository<T : Any, ID : Any>(
    private val repository: Repository<T, ID>,
    protected val cache: MutableMap<ID, T> = synchronizedMap(ReferenceMap()),
    loader: CacheLoader<T, ID> = CacheLoaderAdapter(repository)
) : Repository<T, ID> {
    private val index = CacheIndex(cache, loader)

    override suspend fun create(entity: T): T {
        return repository.create(entity)
    }

    override fun createAll(entities: Flow<T>): Flow<T> {
        return repository.createAll(entities)
    }

    override fun createAll(entities: Iterable<T>): Flow<T> {
        return repository.createAll(entities)
    }

    override suspend fun existsById(id: ID): Boolean {
        return repository.existsById(id)
    }

    override suspend fun findById(id: ID): T? {
        return index.findByKey(id)
    }

    override fun findAll(): Flow<T> {
        return repository.findAll()
    }

    override fun findAllById(ids: Iterable<ID>): Flow<T> {
        return index.findAllByKey(ids)
    }

    override suspend fun updateById(id: ID, patch: Patch<T>): T? {
        return repository.updateById(id, patch)?.also { cache[id] = it }
    }

    override suspend fun updateById(id: ID, patch: AsyncPatch<T>): T? {
        return repository.updateById(id, patch)?.also { cache[id] = it }
    }

    override suspend fun update(entity: T): T? {
        return repository.update(entity)
    }

    override suspend fun update(entity: T, patch: Patch<T>): T? {
        return repository.update(entity, patch)
    }

    override suspend fun update(entity: T, patch: AsyncPatch<T>): T? {
        return repository.update(entity, patch)
    }

    override fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T?> {
        return repository.updateAllById(ids, patch)
    }

    override fun updateAllById(ids: Iterable<ID>, patch: AsyncPatch<T>): Flow<T?> {
        return repository.updateAllById(ids, patch)
    }

    override fun updateAll(entity: Iterable<T>): Flow<T?> {
        return repository.updateAll(entity)
    }

    override fun updateAll(entity: Iterable<T>, patch: Patch<T>): Flow<T?> {
        return repository.updateAll(entity, patch)
    }

    override fun updateAll(entity: Iterable<T>, patch: AsyncPatch<T>): Flow<T?> {
        return repository.updateAll(entity, patch)
    }

    override suspend fun count(): Long {
        return repository.count()
    }

    override suspend fun deleteById(id: ID) {
        repository.deleteById(id)
        cache.remove(id)
    }

    override suspend fun delete(entity: T) {
        return repository.delete(entity)
    }

    override suspend fun deleteAllById(ids: Iterable<ID>) {
        repository.deleteAllById(ids)
        ids.forEach { cache.remove(it) }
    }

    override suspend fun deleteAll(entities: Iterable<T>) {
        return repository.deleteAll(entities)
    }

    override suspend fun deleteAll() {
        repository.deleteAll()
        cache.clear()
    }
}
