package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.Cloneable
import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.repository.cache.CacheLoader
import io.github.siyual_park.data.repository.cache.CachedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.apache.commons.collections4.map.ReferenceMap
import java.util.Collections.synchronizedMap

open class R2DBCCachedRepository<T : Cloneable<T>, ID : Any>(
    private val repository: R2DBCRepository<T, ID>,
    cache: MutableMap<ID, T> = synchronizedMap(ReferenceMap()),
    loader: CacheLoader<T, ID> = R2DBCCacheLoaderAdapter(repository)
) : CachedRepository<T, ID>(repository, cache, loader) {
    private val entityManager = repository.entityManager

    override suspend fun create(entity: T): T {
        return repository.create(entity)
            .also { cache[entityManager.getId(entity)] = entity }
    }

    override fun createAll(entities: Flow<T>): Flow<T> {
        return repository.createAll(entities)
            .map { it.also { cache[entityManager.getId(it)] = it } }
    }

    override fun createAll(entities: Iterable<T>): Flow<T> {
        return repository.createAll(entities)
            .map { it.also { cache[entityManager.getId(it)] = it } }
    }

    override suspend fun existsById(id: ID): Boolean {
        return repository.existsById(id)
    }

    override fun findAll(): Flow<T> {
        return repository.findAll()
            .map { it.also { cache[entityManager.getId(it)] = it } }
    }

    override suspend fun update(entity: T): T? {
        return repository.update(entity)
            ?.also { cache[entityManager.getId(it)] = it }
    }

    override suspend fun update(entity: T, patch: Patch<T>): T? {
        return repository.update(entity, patch)
            ?.also { cache[entityManager.getId(it)] = it }
    }

    override suspend fun update(entity: T, patch: AsyncPatch<T>): T? {
        return repository.update(entity, patch)
            ?.also { cache[entityManager.getId(it)] = it }
    }

    override fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T?> {
        return repository.updateAllById(ids, patch)
            .map { it?.also { cache[entityManager.getId(it)] = it } }
    }

    override fun updateAllById(ids: Iterable<ID>, patch: AsyncPatch<T>): Flow<T?> {
        return repository.updateAllById(ids, patch)
            .map { it?.also { cache[entityManager.getId(it)] = it } }
    }

    override fun updateAll(entity: Iterable<T>): Flow<T?> {
        return repository.updateAll(entity)
            .map { it?.also { cache[entityManager.getId(it)] = it } }
    }

    override fun updateAll(entity: Iterable<T>, patch: Patch<T>): Flow<T?> {
        return repository.updateAll(entity, patch)
            .map { it?.also { cache[entityManager.getId(it)] = it } }
    }

    override fun updateAll(entity: Iterable<T>, patch: AsyncPatch<T>): Flow<T?> {
        return repository.updateAll(entity, patch)
            .map { it?.also { cache[entityManager.getId(it)] = it } }
    }

    override suspend fun delete(entity: T) {
        return repository.delete(entity)
            .also { cache.remove(entityManager.getId(entity)) }
    }

    override suspend fun deleteAll(entities: Iterable<T>) {
        entities.map { cache.remove(entityManager.getId(it)) }
        return repository.deleteAll(entities)
    }
}
