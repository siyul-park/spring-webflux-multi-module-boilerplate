package io.github.siyual_park.data.cache

import io.github.siyual_park.data.WeekProperty
import io.github.siyual_park.util.Reversed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import java.util.Collections

class MultiLevelStorage<ID : Any, T : Any>(
    root: Storage<ID, T>
) : Storage<ID, T> {
    private val storages = Collections.synchronizedList(mutableListOf<Storage<ID, T>>())

    init {
        storages.add(root)
    }

    fun register(storage: Storage<ID, T>): MultiLevelStorage<ID, T> {
        storages.add(storage)
        return this
    }

    override suspend fun <KEY : Any> createIndex(name: String, property: WeekProperty<T, KEY>) {
        storages.forEach { it.createIndex(name, property) }
    }

    override suspend fun removeIndex(name: String) {
        storages.forEach { it.removeIndex(name) }
    }

    override suspend fun containsIndex(name: String): Boolean {
        return storages.all { it.containsIndex(name) }
    }

    override suspend fun getIndexes(): Map<String, WeekProperty<T, *>> {
        return rootStorage().getIndexes()
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY): T? {
        for (i in storages.indices.reversed()) {
            val storage = storages[i]
            val value = storage.getIfPresent(index, key)
            if (value != null) {
                for (j in i + 1 until storages.size) {
                    storages[j].add(value)
                }
                return value
            }
        }

        return null
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY, loader: suspend () -> T?): T? {
        return getIfPresent(index, key) ?: loader()?.also { add(it) }
    }

    override suspend fun getIfPresent(id: ID): T? {
        for (i in storages.indices.reversed()) {
            val storage = storages[i]
            val value = storage.getIfPresent(id)
            if (value != null) {
                for (j in i + 1 until storages.size) {
                    storages[j].add(value)
                }
                return value
            }
        }

        return null
    }

    override suspend fun getIfPresent(id: ID, loader: suspend () -> T?): T? {
        return getIfPresent(id) ?: loader()?.also { add(it) }
    }

    @Suppress("NAME_SHADOWING")
    override fun <KEY : Any> getAll(index: String, keys: Iterable<KEY>): Flow<T?> {
        return flow {
            val keys = keys.toList()
            val result = MutableList<T?>(keys.size) { null }

            for (i in storages.indices.reversed()) {
                val storage = storages[i]
                val value = storage.getAll(index, keys).toList()

                value.forEachIndexed { index, it ->
                    if (it != null) {
                        result[index] = it
                    }
                }

                if (result.filterNotNull().size == keys.size) {
                    emitAll(result.asFlow())
                    return@flow
                }
            }
            emitAll(result.asFlow())
        }
    }

    @Suppress("NAME_SHADOWING")
    override fun getAll(ids: Iterable<ID>): Flow<T?> {
        return flow {
            val ids = ids.toList()
            val result = MutableList<T?>(ids.size) { null }

            for (i in storages.indices.reversed()) {
                val storage = storages[i]
                val value = storage.getAll(ids).toList()

                value.forEachIndexed { index, it ->
                    if (it != null) {
                        result[index] = it
                    }
                }

                if (result.filterNotNull().size == ids.size) {
                    emitAll(result.asFlow())
                    return@flow
                }
            }
            emitAll(result.asFlow())
        }
    }

    override suspend fun remove(id: ID) {
        for (storage in Reversed(storages)) {
            storage.remove(id)
        }
    }

    override suspend fun add(entity: T) {
        for (storage in Reversed(storages)) {
            storage.add(entity)
        }
    }

    override suspend fun clear() {
        for (storage in Reversed(storages)) {
            storage.clear()
        }
    }

    override suspend fun entries(): Set<Pair<ID, T>> {
        return rootStorage().entries()
    }

    override suspend fun status(): Status {
        return storages.fold(Status(0, 0)) { acc, storage ->
            val cur = storage.status()
            Status(acc.hit + cur.hit, acc.miss + cur.miss)
        }
    }

    private fun rootStorage(): Storage<ID, T> {
        return storages.elementAt(0)
    }
}
