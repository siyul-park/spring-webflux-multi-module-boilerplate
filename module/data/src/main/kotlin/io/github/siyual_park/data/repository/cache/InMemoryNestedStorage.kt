package io.github.siyual_park.data.repository.cache

import com.google.common.cache.CacheBuilder
import com.google.common.collect.Sets

@Suppress("UNCHECKED_CAST")
class InMemoryNestedStorage<T : Any, ID : Any>(
    private val cacheBuilder: () -> CacheBuilder<ID, T>,
    private val idExtractor: Extractor<T, ID>,
    override val parent: NestedStorage<T, ID>? = null
) : NestedStorage<T, ID> {
    private val localStorage = InMemoryStorage(cacheBuilder(), idExtractor)
    private val additionalRemoved = Sets.newConcurrentHashSet<ID>()

    override fun diff(): Pair<Set<T>, Set<ID>> {
        return localStorage.entries().values.toSet() to additionalRemoved
    }

    override fun fork(): NestedStorage<T, ID> {
        return InMemoryNestedStorage(
            cacheBuilder,
            idExtractor,
            this
        ).also {
            getExtractors().forEach { (name, extractor) ->
                it.createIndex(name, extractor as Extractor<T, Any>)
            }
        }
    }

    override fun join(storage: NestedStorage<T, ID>) {
        val (created, removed) = storage.diff()
        storage.clear()

        removed.forEach {
            remove(it)
        }
        created.forEach {
            put(it)
        }
    }

    override fun <KEY : Any> createIndex(name: String, extractor: Extractor<T, KEY>) {
        localStorage.createIndex(name, extractor)
    }

    override fun removeIndex(name: String) {
        localStorage.removeIndex(name)
    }

    override fun getExtractors(): Map<String, Extractor<T, *>> {
        return localStorage.getExtractors()
    }

    override fun containsIndex(name: String): Boolean {
        return localStorage.containsIndex(name)
    }

    override fun <KEY : Any> getIfPresent(key: KEY, index: String): T? {
        return localStorage.getIfPresent(key, index)
    }

    override fun <KEY : Any> getIfPresent(key: KEY, index: String, loader: () -> T?): T? {
        return localStorage.getIfPresent(key, index) {
            loader()?.also { invalidRemoved(it) }
        }
    }

    override suspend fun <KEY : Any> getIfPresentAsync(key: KEY, index: String, loader: suspend () -> T?): T? {
        return localStorage.getIfPresentAsync(key, index) {
            loader()?.also { invalidRemoved(it) }
        }
    }

    override fun getIfPresent(id: ID): T? {
        return localStorage.getIfPresent(id)
    }

    override fun getIfPresent(id: ID, loader: () -> T?): T? {
        return localStorage.getIfPresent(id) {
            loader()?.also { invalidRemoved(it) }
        }
    }

    override suspend fun getIfPresentAsync(id: ID, loader: suspend () -> T?): T? {
        return localStorage.getIfPresentAsync(id) {
            loader()?.also { invalidRemoved(it) }
        }
    }

    override fun remove(id: ID) {
        localStorage.remove(id)
        validRemoved(id)
    }

    override fun delete(entity: T) {
        val id = idExtractor.getKey(entity) ?: return
        remove(id)
    }

    override fun put(entity: T) {
        invalidRemoved(entity)
        localStorage.put(entity)
    }

    override fun clear() {
        localStorage.clear()
        additionalRemoved.clear()
    }

    private fun invalidRemoved(entity: T) {
        if (parent != null) {
            val id = idExtractor.getKey(entity) ?: return
            additionalRemoved.remove(id)
        }
    }

    private fun validRemoved(id: ID) {
        if (parent != null) {
            additionalRemoved.add(id)
        }
    }
}
