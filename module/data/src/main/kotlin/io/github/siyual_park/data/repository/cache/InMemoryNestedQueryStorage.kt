package io.github.siyual_park.data.repository.cache

import com.google.common.cache.CacheBuilder

class InMemoryNestedQueryStorage<T : Any>(
    private val cacheBuilder: (() -> CacheBuilder<Any, Any>),
    override val parent: NestedQueryStorage<T>? = null
) : NestedQueryStorage<T> {
    private val singleCacheProvider = CacheProvider<String, T>(cacheBuilder())
    private val multiCacheProvider = CacheProvider<SelectQuery, Collection<T>>(cacheBuilder())

    override suspend fun getIfPresent(where: String): T? {
        return parent?.getIfPresent(where) ?: singleCacheProvider.getIfPresent(where)
    }

    override suspend fun getIfPresent(where: String, loader: suspend () -> T?): T? {
        return parent?.getIfPresent(where) ?: singleCacheProvider.getIfPresent(where, loader)
    }

    override suspend fun getIfPresent(select: SelectQuery): Collection<T>? {
        return parent?.getIfPresent(select) ?: multiCacheProvider.getIfPresent(select)
    }

    override suspend fun getIfPresent(select: SelectQuery, loader: suspend () -> Collection<T>?): Collection<T>? {
        return parent?.getIfPresent(select) ?: multiCacheProvider.getIfPresent(select, loader)
    }

    override suspend fun clear() {
        singleCacheProvider.clear()
        multiCacheProvider.clear()

        parent?.clear()
    }

    override suspend fun diff(): Pair<Set<Pair<String, T>>, Set<Pair<SelectQuery, Collection<T>>>> {
        return singleCacheProvider.entries() to multiCacheProvider.entries()
    }

    override suspend fun fork(): NestedQueryStorage<T> {
        return InMemoryNestedQueryStorage(cacheBuilder, this)
    }

    override suspend fun merge(storage: NestedQueryStorage<T>) {
        val (single, multi) = storage.diff()
        single.forEach { (key, value) ->
            singleCacheProvider.put(key, value)
        }
        multi.forEach { (key, value) ->
            multiCacheProvider.put(key, value)
        }
        storage.clear()
    }
}
