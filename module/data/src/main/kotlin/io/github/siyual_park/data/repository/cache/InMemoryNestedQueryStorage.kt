package io.github.siyual_park.data.repository.cache

import kotlinx.coroutines.flow.Flow

class InMemoryNestedQueryStorage<T : Any>(
    singleCacheProvider: CacheProvider<String, T?>,
    multiCacheProvider: CacheProvider<SelectQuery, Collection<T>>,
    override val parent: NestedQueryStorage<T>? = null
) : NestedQueryStorage<T> {
    private val delegator = SimpleCachedQueryStorage(
        singleCacheProvider,
        multiCacheProvider
    )

    override suspend fun fork(): NestedQueryStorage<T> {
        return InMemoryNestedQueryStorage(SimpleCacheProvider(), SimpleCacheProvider(), this)
    }

    override suspend fun merge(storage: NestedQueryStorage<T>) {
        clear()
    }

    override suspend fun clear() {
        delegator.clear()
    }

    override suspend fun getIfPresent(where: String): T? {
        return delegator.getIfPresent(where)
    }

    override suspend fun getIfPresent(where: String, loader: suspend () -> T?): T? {
        return delegator.getIfPresent(where, loader)
    }

    override fun getIfPresent(select: SelectQuery): Flow<T> {
        return delegator.getIfPresent(select)
    }

    override fun getIfPresent(select: SelectQuery, loader: () -> Flow<T>): Flow<T> {
        return delegator.getIfPresent(select, loader)
    }
}
