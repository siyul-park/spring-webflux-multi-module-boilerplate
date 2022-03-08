package io.github.siyual_park.data.repository.cache

import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.repository.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import java.util.TreeMap

class SimpleCachedRepository<T : Any, ID : Any>(
    private val repository: Repository<T, ID>,
    override val storage: Storage<T, ID>,
) : CachedRepository<T, ID> {
    override suspend fun create(entity: T): T {
        return repository.create(entity)
            .also { storage.put(it) }
    }

    override fun createAll(entities: Flow<T>): Flow<T> {
        return repository.createAll(entities)
            .onEach { storage.put(it) }
    }

    override fun createAll(entities: Iterable<T>): Flow<T> {
        return repository.createAll(entities)
            .onEach { storage.put(it) }
    }

    override suspend fun existsById(id: ID): Boolean {
        if (storage.getIfPresent(id) != null) {
            return true
        }

        return repository.existsById(id)
            .also {
                if (!it) {
                    storage.removeBy(id)
                }
            }
    }

    override suspend fun findById(id: ID): T? {
        return storage.getIfPresentAsync(id) { repository.findById(id) }
    }

    override fun findAll(): Flow<T> {
        return repository.findAll()
            .onEach { storage.put(it) }
    }

    override fun findAllById(ids: Iterable<ID>): Flow<T> {
        return flow {
            val result = TreeMap<Int, T>()

            val notCachedKey = mutableListOf<Pair<Int, ID>>()
            ids.forEachIndexed { index, key ->
                val cached = storage.getIfPresent(key)
                if (cached == null) {
                    notCachedKey.add(index to key)
                } else {
                    result[index] = cached
                }
            }

            if (notCachedKey.isNotEmpty()) {
                repository.findAllById(notCachedKey.map { it.second })
                    .collectIndexed { index, entity ->
                        val (originIndex, _) = notCachedKey[index]
                        storage.put(entity)
                        result[originIndex] = entity
                    }
            }

            emitAll(result.values.asFlow())
        }
    }

    override suspend fun updateById(id: ID, patch: Patch<T>): T? {
        return repository.updateById(id, patch)
            ?.also { storage.put(it) }
    }

    override suspend fun updateById(id: ID, patch: AsyncPatch<T>): T? {
        return repository.updateById(id, patch)
            ?.also { storage.put(it) }
    }

    override suspend fun update(entity: T): T? {
        return repository.update(entity)
            ?.also { storage.put(it) }
    }

    override suspend fun update(entity: T, patch: Patch<T>): T? {
        return repository.update(entity, patch)
            ?.also { storage.put(it) }
    }

    override suspend fun update(entity: T, patch: AsyncPatch<T>): T? {
        return repository.update(entity, patch)
            ?.also { storage.put(it) }
    }

    override fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T?> {
        return repository.updateAllById(ids, patch)
            .onEach { it?.let { storage.put(it) } }
    }

    override fun updateAllById(ids: Iterable<ID>, patch: AsyncPatch<T>): Flow<T?> {
        return repository.updateAllById(ids, patch)
            .onEach { it?.let { storage.put(it) } }
    }

    override fun updateAll(entity: Iterable<T>): Flow<T?> {
        return repository.updateAll(entity)
            .onEach { it?.let { storage.put(it) } }
    }

    override fun updateAll(entity: Iterable<T>, patch: Patch<T>): Flow<T?> {
        return repository.updateAll(entity, patch)
            .onEach { it?.let { storage.put(it) } }
    }

    override fun updateAll(entity: Iterable<T>, patch: AsyncPatch<T>): Flow<T?> {
        return repository.updateAll(entity, patch)
            .onEach { it?.let { storage.put(it) } }
    }

    override suspend fun count(): Long {
        return repository.count()
    }

    override suspend fun deleteById(id: ID) {
        storage.removeBy(id)
        repository.deleteById(id)
    }

    override suspend fun delete(entity: T) {
        storage.remove(entity)
        repository.delete(entity)
    }

    override suspend fun deleteAllById(ids: Iterable<ID>) {
        ids.forEach { storage.removeBy(it) }
        repository.deleteAllById(ids)
    }

    override suspend fun deleteAll(entities: Iterable<T>) {
        entities.forEach { storage.remove(it) }
        repository.deleteAll(entities)
    }

    override suspend fun deleteAll() {
        storage.clear()
        repository.deleteAll()
    }
}
