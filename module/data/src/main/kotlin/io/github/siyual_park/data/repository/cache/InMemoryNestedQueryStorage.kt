package io.github.siyual_park.data.repository.cache

import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.flow.Flow

class InMemoryNestedQueryStorage<T : Any>(
    private val cacheBuilder: () -> CacheBuilder<Any, Any>,
    override val parent: NestedQueryStorage<T>? = null,
) : NestedQueryStorage<T> {
    private val delegator = SimpleCachedQueryStorage<T>(cacheBuilder)
    private var cleared = false

    override suspend fun fork(): NestedQueryStorage<T> {
        return InMemoryNestedQueryStorage(
            cacheBuilder,
            this
        )
    }

    override suspend fun merge(storage: NestedQueryStorage<T>) {
        if (storage.isCleared()) {
            clear()
        }
    }

    override fun isCleared(): Boolean {
        return cleared
    }

    override suspend fun clear() {
        cleared = true
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
