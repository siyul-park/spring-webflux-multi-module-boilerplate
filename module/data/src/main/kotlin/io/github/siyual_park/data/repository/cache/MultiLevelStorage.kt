package io.github.siyual_park.data.repository.cache

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

    override fun <KEY : Any> getIfPresent(index: String, key: KEY): T? {
        for (storage in Reversed(storages)) {
            val value = storage.getIfPresent(index, key)
            if (value != null) {
                return value
            }
        }

        return null
    }

    override fun <KEY : Any> getIfPresent(index: String, key: KEY, loader: () -> T?): T? {
        val value = getIfPresent(index, key)
        if (value != null) {
            return value
        }

        return loader()?.also { put(it) }
    }

    override suspend fun <KEY : Any> getIfPresentAsync(index: String, key: KEY, loader: suspend () -> T?): T? {
        val value = getIfPresent(index, key)
        if (value != null) {
            return value
        }

        return loader()?.also { put(it) }
    }

    override fun getIfPresent(id: ID): T? {
        for (storage in Reversed(storages)) {
            val value = storage.getIfPresent(id)
            if (value != null) {
                return value
            }
        }

        return null
    }

    override fun getIfPresent(id: ID, loader: () -> T?): T? {
        val value = getIfPresent(id)
        if (value != null) {
            return value
        }

        return loader()?.also { put(it) }
    }

    override suspend fun getIfPresentAsync(id: ID, loader: suspend () -> T?): T? {
        val value = getIfPresent(id)
        if (value != null) {
            return value
        }

        return loader()?.also { put(it) }
    }

    override fun remove(id: ID) {
        for (storage in Reversed(storages)) {
            storage.remove(id)
        }
    }

    override fun delete(entity: T) {
        for (storage in Reversed(storages)) {
            storage.delete(entity)
        }
    }

    override fun put(entity: T) {
        for (storage in Reversed(storages)) {
            storage.put(entity)
        }
    }

    override fun clear() {
        for (storage in Reversed(storages)) {
            storage.clear()
        }
    }

    private fun rootStorage(): Storage<T, ID> {
        return storages.elementAt(0)
    }
}
