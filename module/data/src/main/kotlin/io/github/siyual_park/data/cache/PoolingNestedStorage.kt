package io.github.siyual_park.data.cache

import com.google.common.collect.Sets
import io.github.siyual_park.data.repository.Extractor

@Suppress("UNCHECKED_CAST", "NAME_SHADOWING")
class PoolingNestedStorage<ID : Any, T : Any>(
    private val pool: Pool<Storage<ID, T>>,
    private val idExtractor: Extractor<T, ID>,
    override val parent: NestedStorage<ID, T>? = null
) : NestedStorage<ID, T> {
    private val delegator = SuspendLazy { pool.pop().also { it.clear() } }

    private val removed = if (parent == null) {
        null
    } else {
        Sets.newConcurrentHashSet<ID>()
    }

    override suspend fun checkout(): Map<ID, T?> {
        val map = mutableMapOf<ID, T?>()
        delegator.get().entries().forEach {
            map[it.first] = it.second
        }
        removed?.forEach {
            map[it] = null
        }

        clear()

        return map
    }

    override suspend fun fork(): NestedStorage<ID, T> {
        return PoolingNestedStorage(
            pool,
            idExtractor,
            this
        ).also {
            getExtractors().forEach { (name, extractor) ->
                it.createIndex(name, extractor as Extractor<T, Any>)
            }
        }
    }

    override suspend fun merge(storage: NestedStorage<ID, T>) {
        val diff = storage.checkout()

        diff.forEach { (key, value) ->
            if (value != null) {
                add(value)
            } else {
                remove(key)
            }
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
        return getIfPresent(index, key) ?: loader()?.also { add(it) }
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY): T? {
        return delegator.get().getIfPresent(index, key) ?: guard { parent?.getIfPresent(index, key) }
    }

    override suspend fun getIfPresent(id: ID, loader: suspend () -> T?): T? {
        return getIfPresent(id) ?: loader()?.also { add(it) }
    }

    override suspend fun getIfPresent(id: ID): T? {
        return delegator.get().getIfPresent(id) ?: guard { parent?.getIfPresent(id) }
    }

    override suspend fun remove(id: ID) {
        delegator.get().remove(id)
        removed?.add(id)
    }

    override suspend fun add(entity: T) {
        delegator.get().add(entity)
        removed?.remove(idExtractor.getKey(entity))
    }

    override suspend fun clear() {
        removed?.clear()
        delegator.pop()?.let {
            it.clear()
            pool.push(it)
        }
    }

    override suspend fun entries(): Set<Pair<ID, T>> {
        return delegator.get().entries()
    }

    private suspend fun guard(loader: suspend () -> T?): T? {
        return loader()?.let {
            if (removed == null || !removed.contains(idExtractor.getKey(it))) {
                it
            } else {
                null
            }
        }
    }
}
