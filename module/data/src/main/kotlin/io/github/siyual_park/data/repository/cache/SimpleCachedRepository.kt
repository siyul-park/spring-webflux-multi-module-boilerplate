package io.github.siyual_park.data.repository.cache

import io.github.siyual_park.data.Extractor
import io.github.siyual_park.data.cache.Storage
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
    private val delegator: Repository<T, ID>,
    private val storage: Storage<ID, T>,
    private val idExtractor: Extractor<T, ID>
) : Repository<T, ID> {

    override suspend fun create(entity: T): T {
        return delegator.create(entity)
            .also { storage.add(it) }
    }

    override fun createAll(entities: Flow<T>): Flow<T> {
        return flow {
            emitAll(
                delegator.createAll(entities)
                    .onEach { storage.add(it) }
            )
        }
    }

    override fun createAll(entities: Iterable<T>): Flow<T> {
        return flow {
            emitAll(
                delegator.createAll(entities)
                    .onEach { storage.add(it) }
            )
        }
    }

    override suspend fun existsById(id: ID): Boolean {
        if (storage.getIfPresent(id) != null) {
            return true
        }

        return delegator.existsById(id)
            .also {
                if (!it) {
                    storage.remove(id)
                }
            }
    }

    override suspend fun findById(id: ID): T? {
        return storage.getIfPresent(id) { delegator.findById(id) }
    }

    override fun findAll(): Flow<T> {
        return flow {
            emitAll(
                delegator.findAll()
                    .onEach { storage.add(it) }
            )
        }
    }

    override fun findAllById(ids: Iterable<ID>): Flow<T> {
        return flow {
            val result = mutableListOf<T>()
            val notCachedIds = mutableListOf<ID>()
            ids.forEach { id ->
                val cached = storage.getIfPresent(id)
                if (cached == null) {
                    notCachedIds.add(id)
                } else {
                    result.add(cached)
                }
            }

            if (notCachedIds.isNotEmpty()) {
                delegator.findAllById(notCachedIds)
                    .onEach { storage.add(it) }
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
        return delegator.updateById(id, patch)
            ?.also { storage.add(it) }
    }

    override suspend fun updateById(id: ID, patch: AsyncPatch<T>): T? {
        return delegator.updateById(id, patch)
            ?.also { storage.add(it) }
    }

    override suspend fun update(entity: T): T? {
        return delegator.update(entity)
            ?.also { storage.add(it) }
    }

    override suspend fun update(entity: T, patch: Patch<T>): T? {
        return delegator.update(entity, patch)
            ?.also { storage.add(it) }
    }

    override suspend fun update(entity: T, patch: AsyncPatch<T>): T? {
        return delegator.update(entity, patch)
            ?.also { storage.add(it) }
    }

    override fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T?> {
        return flow {
            emitAll(
                delegator.updateAllById(ids, patch)
                    .onEach { it?.let { storage.add(it) } }
            )
        }
    }

    override fun updateAllById(ids: Iterable<ID>, patch: AsyncPatch<T>): Flow<T?> {
        return flow {
            emitAll(
                delegator.updateAllById(ids, patch)
                    .onEach { it?.let { storage.add(it) } }
            )
        }
    }

    override fun updateAll(entity: Iterable<T>): Flow<T?> {
        return flow {
            emitAll(
                delegator.updateAll(entity)
                    .onEach { it?.let { storage.add(it) } }
            )
        }
    }

    override fun updateAll(entity: Iterable<T>, patch: Patch<T>): Flow<T?> {
        return flow {
            emitAll(
                delegator.updateAll(entity, patch)
                    .onEach { it?.let { storage.add(it) } }
            )
        }
    }

    override fun updateAll(entity: Iterable<T>, patch: AsyncPatch<T>): Flow<T?> {
        return flow {
            emitAll(
                delegator.updateAll(entity, patch)
                    .onEach { it?.let { storage.add(it) } }
            )
        }
    }

    override suspend fun count(): Long {
        return delegator.count()
    }

    override suspend fun deleteById(id: ID) {
        storage.remove(id)
        delegator.deleteById(id)
    }

    override suspend fun delete(entity: T) {
        idExtractor.getKey(entity)?.let { storage.remove(it) }
        delegator.delete(entity)
    }

    override suspend fun deleteAllById(ids: Iterable<ID>) {
        ids.forEach { storage.remove(it) }
        delegator.deleteAllById(ids)
    }

    override suspend fun deleteAll(entities: Iterable<T>) {
        entities.forEach { idExtractor.getKey(it)?.let { id -> storage.remove(id) } }
        delegator.deleteAll(entities)
    }

    override suspend fun deleteAll() {
        storage.clear()
        delegator.deleteAll()
    }
}
