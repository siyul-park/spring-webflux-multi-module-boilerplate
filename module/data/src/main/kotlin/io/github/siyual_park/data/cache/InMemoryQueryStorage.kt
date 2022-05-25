package io.github.siyual_park.data.cache

import com.google.common.cache.CacheBuilder

class InMemoryQueryStorage<T : Any>(
    cacheBuilder: (() -> CacheBuilder<Any, Any>)
) : QueryStorage<T> {
    private val multiCache = CacheProvider<SelectQuery, Collection<T>>(cacheBuilder())

    override suspend fun getIfPresent(select: SelectQuery): Collection<T>? {
        return multiCache.getIfPresent(select)
    }

    override suspend fun getIfPresent(select: SelectQuery, loader: suspend () -> Collection<T>?): Collection<T>? {
        return multiCache.getIfPresent(select, loader)
    }

    override suspend fun remove(select: SelectQuery) {
        multiCache.remove(select)
    }

    override suspend fun put(select: SelectQuery, value: Collection<T>) {
        multiCache.put(select, value)
    }

    override suspend fun clear() {
        multiCache.clear()
    }

    override suspend fun entries(): Set<Pair<SelectQuery, Collection<T>>> {
        return multiCache.entries()
    }
}
