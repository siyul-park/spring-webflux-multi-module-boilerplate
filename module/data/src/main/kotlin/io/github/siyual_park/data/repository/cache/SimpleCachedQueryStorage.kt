package io.github.siyual_park.data.repository.cache

class SimpleCachedQueryStorage<T : Any>(
    private val singleCacheProvider: CacheProvider<String, T?>,
    private val multiCacheProvider: CacheProvider<SelectQuery, Collection<T>>
) : QueryStorage<T> {

    override suspend fun getIfPresent(where: String): T? {
        return singleCacheProvider.getIfPresent(where)
    }

    override suspend fun getIfPresent(where: String, loader: suspend () -> T?): T? {
        return singleCacheProvider.getIfPresent(where, loader)
    }

    override suspend fun clear() {
        singleCacheProvider.clear()
        multiCacheProvider.clear()
    }

    override suspend fun getIfPresent(select: SelectQuery): Collection<T>? {
        return multiCacheProvider.getIfPresent(select)
    }

    override suspend fun getIfPresent(select: SelectQuery, loader: suspend () -> Collection<T>?): Collection<T>? {
        return multiCacheProvider.getIfPresent(select, loader)
    }
}
