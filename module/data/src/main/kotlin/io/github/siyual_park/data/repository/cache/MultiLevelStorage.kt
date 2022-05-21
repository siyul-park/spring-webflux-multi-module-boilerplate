package io.github.siyual_park.data.repository.cache

import io.github.siyual_park.data.repository.Extractor
import io.github.siyual_park.util.Reversed
import java.util.Collections

class MultiLevelStorage<T : Any, ID : Any>(
    root: Storage<T, ID>
) : Storage<T, ID> {
    override val idExtractor: Extractor<T, ID> = root.idExtractor

    private val storages = Collections.synchronizedList(mutableListOf<Storage<T, ID>>())

    init {
        storages.add(root)
    }

    fun register(storage: Storage<T, ID>): MultiLevelStorage<T, ID> {
        storages.add(storage)
        return this
    }

    override fun <KEY : Any> createIndex(name: String, extractor: Extractor<T, KEY>) {
        storages.forEach { it.createIndex(name, extractor) }
    }

    override fun removeIndex(name: String) {
        storages.forEach { it.removeIndex(name) }
    }

    override fun containsIndex(name: String): Boolean {
        return storages.all { it.containsIndex(name) }
    }

    override fun getExtractors(): Map<String, Extractor<T, *>> {
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

        return loader()?.also { put(it) }
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

        return loader()?.also { put(it) }
    }

    override suspend fun remove(id: ID) {
        for (storage in Reversed(storages)) {
            storage.remove(id)
        }
    }

    override suspend fun delete(entity: T) {
        for (storage in Reversed(storages)) {
            storage.delete(entity)
        }
    }

    override suspend fun put(entity: T) {
        for (storage in Reversed(storages)) {
            storage.put(entity)
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

    private fun rootStorage(): Storage<T, ID> {
        return storages.elementAt(0)
    }
}
