package io.github.siyual_park.data.cache

import com.google.common.collect.Sets
import io.github.siyual_park.data.WeekProperty

@Suppress("UNCHECKED_CAST", "NAME_SHADOWING")
class PoolingNestedStorage<ID : Any, T : Any>(
    private val pool: Pool<Storage<ID, T>>,
    private val id: WeekProperty<T, ID?>,
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
            id,
            this
        ).also {
            getIndexes().forEach { (name, extractor) ->
                it.createIndex(name, extractor as WeekProperty<T, Any>)
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

    override suspend fun <KEY : Any> createIndex(name: String, property: WeekProperty<T, KEY>) {
        delegator.get().createIndex(name, property)
    }

    override suspend fun removeIndex(name: String) {
        delegator.get().removeIndex(name)
    }

    override suspend fun getIndexes(): Map<String, WeekProperty<T, *>> {
        return delegator.get().getIndexes()
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
        removed?.remove(id.get(entity))
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
            if (removed == null || !removed.contains(id.get(it))) {
                it
            } else {
                null
            }
        }
    }
}
