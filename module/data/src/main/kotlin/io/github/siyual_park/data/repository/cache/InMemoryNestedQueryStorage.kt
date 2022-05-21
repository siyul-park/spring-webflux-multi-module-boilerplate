package io.github.siyual_park.data.repository.cache

class InMemoryNestedQueryStorage<T : Any>(
    private val singleCacheProvider: CacheProvider<String, T>,
    private val multiCacheProvider: CacheProvider<SelectQuery, Collection<T>>,
    override val parent: NestedQueryStorage<T>? = null
) : NestedQueryStorage<T> {
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
        return InMemoryNestedQueryStorage(SimpleCacheProvider(), SimpleCacheProvider(), this)
    }

    override suspend fun merge(storage: NestedQueryStorage<T>) {
        val (single, multi) = storage.diff()
        single.forEach { (key, value) ->
            singleCacheProvider.put(key, value)
        }
        multi.forEach { (key, value) ->
            multiCacheProvider.put(key, value)
        }
    }
}
