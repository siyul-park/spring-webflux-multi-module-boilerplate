package io.github.siyual_park.data.cache

import com.google.common.cache.CacheBuilder

class InMemoryQueryStorage<T : Any>(
    cacheBuilder: (() -> CacheBuilder<Any, Any>)
) : QueryStorage<T> {
    private val singleCache = CacheProvider<String, T>(cacheBuilder())
    private val multiCache = CacheProvider<SelectQuery, Collection<T>>(cacheBuilder())

    override suspend fun getIfPresent(where: String): T? {
        return singleCache.getIfPresent(where)
    }

    override suspend fun getIfPresent(where: String, loader: suspend () -> T?): T? {
        return singleCache.getIfPresent(where, loader)
    }

    override suspend fun getIfPresent(select: SelectQuery): Collection<T>? {
        return multiCache.getIfPresent(select)
    }

    override suspend fun getIfPresent(select: SelectQuery, loader: suspend () -> Collection<T>?): Collection<T>? {
        return multiCache.getIfPresent(select, loader)
    }

    override suspend fun remove(where: String) {
        singleCache.remove(where)
    }

    override suspend fun remove(select: SelectQuery) {
        multiCache.remove(select)
    }

    override suspend fun put(where: String, value: T) {
        singleCache.put(where, value)
    }

    override suspend fun put(select: SelectQuery, value: Collection<T>) {
        multiCache.put(select, value)
    }

    override suspend fun clear() {
        singleCache.clear()
        multiCache.clear()
    }

    override suspend fun entries(): Pair<Set<Pair<String, T>>, Set<Pair<SelectQuery, Collection<T>>>> {
        return singleCache.entries() to multiCache.entries()
    }
}