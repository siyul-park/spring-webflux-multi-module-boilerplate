package io.github.siyual_park.data.repository.cache

import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.repository.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

class SimpleCachedRepository<T : Any, ID : Any>(
    private val repository: Repository<T, ID>,
    override val storageManager: StorageManager<T, ID>,
    private val idExtractor: Extractor<T, ID>
) : CachedRepository<T, ID> {

    override suspend fun create(entity: T): T {
        val storage = storageManager.getCurrent()
        return repository.create(entity)
            .also { storage.put(it) }
    }

    override fun createAll(entities: Flow<T>): Flow<T> {
        return flow {
            val storage = storageManager.getCurrent()
            emitAll(
                repository.createAll(entities)
                    .onEach { storage.put(it) }
            )
        }
    }

    override fun createAll(entities: Iterable<T>): Flow<T> {
        return flow {
            val storage = storageManager.getCurrent()
            emitAll(
                repository.createAll(entities)
                    .onEach { storage.put(it) }
            )
        }
    }

    override suspend fun existsById(id: ID): Boolean {
        val storage = storageManager.getCurrent()
        if (storage.getIfPresent(id) != null) {
            return true
        }

        return repository.existsById(id)
            .also {
                if (!it) {
                    storage.remove(id)
                }
            }
    }

    override suspend fun findById(id: ID): T? {
        val storage = storageManager.getCurrent()
        return storage.getIfPresentAsync(id) { repository.findById(id) }
    }

    override fun findAll(): Flow<T> {
        return flow {
            val storage = storageManager.getCurrent()
            repository.findAll()
                .onEach { storage.put(it) }
        }
    }

    override fun findAllById(ids: Iterable<ID>): Flow<T> {
        return flow {
            val storage = storageManager.getCurrent()
            val result = mutableListOf<T>()
            val notCachedKey = mutableListOf<ID>()
            ids.forEach { key ->
                val cached = storage.getIfPresent(key)
                if (cached == null) {
                    notCachedKey.add(key)
                } else {
                    result.add(cached)
                }
            }

            if (notCachedKey.isNotEmpty()) {
                repository.findAllById(notCachedKey)
                    .collect { result.add(it) }
            }

            emitAll(
                result.also {
                    it.sortWith { p1, p2 ->
                        val p1Id = idExtractor.getKey(p1)
                        val p2Id = idExtractor.getKey(p2)

                        ids.indexOf(p1Id) - ids.indexOf(p2Id)
                    }
                }.asFlow()
            )
        }
    }

    override suspend fun updateById(id: ID, patch: Patch<T>): T? {
        val storage = storageManager.getCurrent()
        return repository.updateById(id, patch)
            ?.also { storage.put(it) }
    }

    override suspend fun updateById(id: ID, patch: AsyncPatch<T>): T? {
        val storage = storageManager.getCurrent()
        return repository.updateById(id, patch)
            ?.also { storage.put(it) }
    }

    override suspend fun update(entity: T): T? {
        val storage = storageManager.getCurrent()
        return repository.update(entity)
            ?.also { storage.put(it) }
    }

    override suspend fun update(entity: T, patch: Patch<T>): T? {
        val storage = storageManager.getCurrent()
        return repository.update(entity, patch)
            ?.also { storage.put(it) }
    }

    override suspend fun update(entity: T, patch: AsyncPatch<T>): T? {
        val storage = storageManager.getCurrent()
        return repository.update(entity, patch)
            ?.also { storage.put(it) }
    }

    override fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T?> {
        return flow {
            val storage = storageManager.getCurrent()
            repository.updateAllById(ids, patch)
                .onEach { it?.let { storage.put(it) } }
        }
    }

    override fun updateAllById(ids: Iterable<ID>, patch: AsyncPatch<T>): Flow<T?> {
        return flow {
            val storage = storageManager.getCurrent()
            repository.updateAllById(ids, patch)
                .onEach { it?.let { storage.put(it) } }
        }
    }

    override fun updateAll(entity: Iterable<T>): Flow<T?> {
        return flow {
            val storage = storageManager.getCurrent()
            repository.updateAll(entity)
                .onEach { it?.let { storage.put(it) } }
        }
    }

    override fun updateAll(entity: Iterable<T>, patch: Patch<T>): Flow<T?> {
        return flow {
            val storage = storageManager.getCurrent()
            repository.updateAll(entity, patch)
                .onEach { it?.let { storage.put(it) } }
        }
    }

    override fun updateAll(entity: Iterable<T>, patch: AsyncPatch<T>): Flow<T?> {
        return flow {
            val storage = storageManager.getCurrent()
            repository.updateAll(entity, patch)
                .onEach { it?.let { storage.put(it) } }
        }
    }

    override suspend fun count(): Long {
        return repository.count()
    }

    override suspend fun deleteById(id: ID) {
        val storage = storageManager.getCurrent()
        storage.remove(id)
        repository.deleteById(id)
    }

    override suspend fun delete(entity: T) {
        val storage = storageManager.getCurrent()
        storage.delete(entity)
        repository.delete(entity)
    }

    override suspend fun deleteAllById(ids: Iterable<ID>) {
        val storage = storageManager.getCurrent()
        ids.forEach { storage.remove(it) }
        repository.deleteAllById(ids)
    }

    override suspend fun deleteAll(entities: Iterable<T>) {
        val storage = storageManager.getCurrent()
        entities.forEach { storage.delete(it) }
        repository.deleteAll(entities)
    }

    override suspend fun deleteAll() {
        val storage = storageManager.getCurrent()
        storage.clear()
        repository.deleteAll()
    }
}
