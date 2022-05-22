package io.github.siyual_park.data.cache

import io.github.siyual_park.data.repository.Extractor
import io.github.siyual_park.util.Reversed
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

    override suspend fun <KEY : Any> createIndex(name: String, extractor: Extractor<T, KEY>) {
        storages.forEach { it.createIndex(name, extractor) }
    }

    override suspend fun removeIndex(name: String) {
        storages.forEach { it.removeIndex(name) }
    }

    override suspend fun containsIndex(name: String): Boolean {
        return storages.all { it.containsIndex(name) }
    }

    override suspend fun getExtractors(): Map<String, Extractor<T, *>> {
        return rootStorage().getExtractors()
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY): T? {
        for (storage in Reversed(storages)) {
            val value = storage.getIfPresent(index, key)
            if (value != null) {
                return value
            }
        }

        return null
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY, loader: suspend () -> T?): T? {
        val value = getIfPresent(index, key)
        if (value != null) {
            return value
        }

        return loader()?.also { add(it) }
    }

    override suspend fun getIfPresent(id: ID): T? {
        for (storage in Reversed(storages)) {
            val value = storage.getIfPresent(id)
            if (value != null) {
                return value
            }
        }

        return null
    }

    override suspend fun getIfPresent(id: ID, loader: suspend () -> T?): T? {
        val value = getIfPresent(id)
        if (value != null) {
            return value
        }

        return loader()?.also { add(it) }
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

    private fun rootStorage(): Storage<ID, T> {
        return storages.elementAt(0)
    }
}
