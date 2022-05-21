package io.github.siyual_park.data.repository.cache

import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.flow.Flow

class InMemoryNestedQueryStorage<T : Any>(
    private val cacheBuilder: () -> CacheBuilder<Any, Any>,
    override val parent: NestedQueryStorage<T>? = null,
) : NestedQueryStorage<T> {
    private val delegator = SimpleCachedQueryStorage<T>(cacheBuilder)

    override suspend fun fork(): NestedQueryStorage<T> {
        return InMemoryNestedQueryStorage(
            cacheBuilder,
            this
        )
    }

    override suspend fun getIfPresent(where: String): T? {
        return parent?.getIfPresent(where) ?: delegator.getIfPresent(where)
    }

    override suspend fun getIfPresent(where: String, loader: suspend () -> T?): T? {
        return parent?.getIfPresent(where) ?: delegator.getIfPresent(where, loader)
    }

    override fun getIfPresent(select: SelectQuery): Flow<T> {
        return parent?.getIfPresent(select) ?: delegator.getIfPresent(select)
    }

    override fun getIfPresent(select: SelectQuery, loader: () -> Flow<T>): Flow<T> {
        return parent?.getIfPresent(select) ?: delegator.getIfPresent(select, loader)
    }

    override suspend fun clear() {
        delegator.clear()
    }
}
