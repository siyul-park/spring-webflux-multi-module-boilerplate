package io.github.siyual_park.data.cache

import com.google.common.collect.Sets
import io.github.siyual_park.data.WeekProperty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList

@Suppress("UNCHECKED_CAST", "NAME_SHADOWING")
class PoolingNestedStorage<ID : Any, T : Any>(
    private val pool: Pool<Storage<ID, T>>,
    private val id: WeekProperty<T, ID?>,
    override val parent: NestedStorage<ID, T>? = null
) : NestedStorage<ID, T> {
    private val delegator = SuspendLazy { pool.pop().also { it.clear() } }
    private val removed = parent?.let { Sets.newConcurrentHashSet<ID>() }

    override suspend fun checkout(): Map<ID, T?> {
        val map = mutableMapOf<ID, T?>()
        delegator.get().entries().forEach { map[it.first] = it.second }
        removed?.forEach { map[it] = null }
        clear()
        return map
    }

    override suspend fun fork(): NestedStorage<ID, T> {
        return PoolingNestedStorage(pool, id, this).also {
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
        return delegator.get().getIfPresent(index, key) ?: load { parent?.getIfPresent(index, key) }
    }

    override suspend fun getIfPresent(id: ID, loader: suspend () -> T?): T? {
        return getIfPresent(id) ?: loader()?.also { add(it) }
    }

    override suspend fun getIfPresent(id: ID): T? {
        return delegator.get().getIfPresent(id) ?: load { parent?.getIfPresent(id) }
    }

    override fun <KEY : Any> getAll(index: String, keys: Iterable<KEY>): Flow<T?> {
        return flow {
            val result = delegator.get().getAll(index, keys).toList().toMutableList()
            if (result.filterNotNull().size != keys.count()) {
                parent?.let {
                    load(it.getAll(index, keys)).toList().forEachIndexed { i, it ->
                        if (it != null) {
                            result[i] = it
                        }
                    }
                }
            }

            emitAll(result.asFlow())
        }
    }

    override fun getAll(ids: Iterable<ID>): Flow<T?> {
        return flow {
            val result = delegator.get().getAll(ids).toList().toMutableList()
            if (result.filterNotNull().size != ids.count()) {
                parent?.let {
                    load(it.getAll(ids)).toList().forEachIndexed { i, it ->
                        if (it != null) {
                            result[i] = it
                        }
                    }
                }
            }

            emitAll(result.asFlow())
        }
    }

    override suspend fun remove(id: ID) {
        delegator.get().remove(id)
        removed?.add(id)
    }

    override suspend fun add(entity: T) {
        val id = id.get(entity)
        delegator.get().add(entity)
        removed?.remove(id)
    }

    override suspend fun clear() {
        removed?.clear()
        delegator.pop()?.let {
            it.clear()
            pool.push(it)
        }
    }

    override suspend fun entries(): Set<Pair<ID, T>> {
        val map = mutableMapOf<ID, T>()
        delegator.get().entries().forEach { map[it.first] = it.second }
        removed?.forEach { map.remove(it) }
        return map.entries.map { (first, second) -> first to second }.toSet()
    }

    override suspend fun status(): Status {
        return pool.used().entries().fold(Status()) { acc, storage ->
            Status.add(acc, storage.status())
        }
    }

    private fun load(loader: Flow<T?>): Flow<T?> {
        return flow { loader.onEach { emit(load { it }) } }
    }

    private suspend fun load(loader: suspend () -> T?): T? {
        val entity = loader() ?: return null
        val id = id.get(entity)
        if (removed?.contains(id) == true) {
            return null
        }
        return entity
    }
}
