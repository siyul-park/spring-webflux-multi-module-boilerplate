package io.github.siyual_park.data.repository.cache

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

class InMemoryNestedQueryStorage<T : Any>(
    singleCacheProvider: CacheProvider<String, T?>,
    multiCacheProvider: CacheProvider<SelectQuery, Collection<T>>
) : NestedQueryStorage<T> {
    override val parent: NestedQueryStorage<T>? = null
    private val children = mutableListOf<NestedQueryStorage<T>>()

    private val delegator = SimpleCachedQueryStorage(
        singleCacheProvider,
        multiCacheProvider
    )

    private var isUpdated = false

    override suspend fun fork(): NestedQueryStorage<T> {
        return InMemoryNestedQueryStorage<T>(SimpleCacheProvider(), SimpleCacheProvider()).also {
            children.add(it)
        }
    }

    override suspend fun merge(storage: NestedQueryStorage<T>) {
        storage.clear()
    }

    override suspend fun clear() {
        if (isUpdated) {
            delegator.clear()
            parent?.clear()
            children.forEach { it.clear() }
            isUpdated = false
        }
    }

    override suspend fun getIfPresent(where: String): T? {
        return parent?.getIfPresent(where) ?: delegator.getIfPresent(where)
    }

    override suspend fun getIfPresent(where: String, loader: suspend () -> T?): T? {
        return parent?.getIfPresent(where) ?: delegator.getIfPresent(where, loader)?.also { isUpdated = true }
    }

    override fun getIfPresent(select: SelectQuery): Flow<T> {
        return parent?.getIfPresent(select) ?: delegator.getIfPresent(select)
    }

    override fun getIfPresent(select: SelectQuery, loader: () -> Flow<T>): Flow<T> {
        return parent?.getIfPresent(select) ?: delegator.getIfPresent(select, loader).onStart { isUpdated = true }
    }
}
