package io.github.siyual_park.data.repository.cache

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
    }

    override suspend fun clear() {
        delegator.clear()
        parent?.clear()
    }

    override suspend fun getIfPresent(where: String): T? {
        return parent?.getIfPresent(where) ?: delegator.getIfPresent(where)
    }

    override suspend fun getIfPresent(where: String, loader: suspend () -> T?): T? {
        return parent?.getIfPresent(where) ?: delegator.getIfPresent(where, loader)
    }

    override suspend fun getIfPresent(select: SelectQuery): Collection<T>? {
        return parent?.getIfPresent(select) ?: delegator.getIfPresent(select)
    }

    override suspend fun getIfPresent(select: SelectQuery, loader: suspend () -> Collection<T>?): Collection<T>? {
        return parent?.getIfPresent(select) ?: delegator.getIfPresent(select, loader)
    }
}
