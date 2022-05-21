package io.github.siyual_park.data.cache

import com.google.common.collect.Sets
import io.github.siyual_park.data.repository.Extractor
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Suppress("UNCHECKED_CAST", "NAME_SHADOWING")
class NestedStorage<T : Any, ID : Any>(
    private val pool: LoadingPool<Storage<T, ID>>,
    private val idExtractor: Extractor<T, ID>,
    override val parent: NestedStorage<T, ID>? = null
) : Storage<T, ID>, GeneralNestedStorage<NestedStorage<T, ID>> {
    private val delegator = AsyncLazy { pool.poll() }
    private val mutex = Mutex()

    private val forceRemoved = Sets.newConcurrentHashSet<ID>()

    suspend fun diff(): Pair<Set<T>, Set<ID>> {
        return delegator.get().entries().map { it.second }.toSet() to forceRemoved.toSet().also {
            clear()
        }
    }

    override suspend fun fork(): NestedStorage<T, ID> {
        return NestedStorage(
            pool,
            idExtractor,
            this
        ).also {
            getExtractors().forEach { (name, extractor) ->
                it.createIndex(name, extractor as Extractor<T, Any>)
            }
        }
    }

    override suspend fun merge(storage: NestedStorage<T, ID>) {
        val (created, removed) = storage.diff()
        removed.forEach {
            remove(it)
        }
        created.forEach {
            put(it)
        }
    }

    override suspend fun <KEY : Any> createIndex(name: String, extractor: Extractor<T, KEY>) {
        delegator.get().createIndex(name, extractor)
    }

    override suspend fun removeIndex(name: String) {
        delegator.get().removeIndex(name)
    }

    override suspend fun getExtractors(): Map<String, Extractor<T, *>> {
        return delegator.get().getExtractors()
    }

    override suspend fun containsIndex(name: String): Boolean {
        return delegator.get().containsIndex(name)
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY, loader: suspend () -> T?): T? {
        return getIfPresent(index, key) ?: loader()?.also { put(it) }
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY): T? {
        return delegator.get().getIfPresent(index, key) ?: guard { parent?.getIfPresent(index, key) }
    }

    override suspend fun getIfPresent(id: ID, loader: suspend () -> T?): T? {
        return getIfPresent(id) ?: loader()?.also { put(it) }
    }

    override suspend fun getIfPresent(id: ID): T? {
        return delegator.get().getIfPresent(id) ?: guard { parent?.getIfPresent(id) }
    }

    override suspend fun remove(id: ID) {
        delegator.get().remove(id)
        forceRemoved.add(id)
    }

    override suspend fun delete(entity: T) {
        idExtractor.getKey(entity)?.let { remove(it) }
    }

    override suspend fun put(entity: T) {
        delegator.get().put(entity)
        forceRemoved.remove(idExtractor.getKey(entity))
    }

    override suspend fun clear() {
        delegator.get().clear()
        forceRemoved.clear()

        mutex.withLock {
            pool.add(delegator.get())
            delegator.clear()
        }
    }

    override suspend fun entries(): Set<Pair<ID, T>> {
        return delegator.get().entries()
    }

    private suspend fun guard(loader: suspend () -> T?): T? {
        return loader()?.let {
            val id = idExtractor.getKey(it)
            if (!forceRemoved.contains(id)) {
                it
            } else {
                null
            }
        }
    }
}
