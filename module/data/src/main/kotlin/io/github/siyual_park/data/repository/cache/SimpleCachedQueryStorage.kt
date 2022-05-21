package io.github.siyual_park.data.repository.cache

import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList

class SimpleCachedQueryStorage<T : Any>(
    cacheBuilder: () -> CacheBuilder<Any, Any>,
) : QueryStorage<T> {
    private val singleCacheProvider = CacheProvider<String, T?>(cacheBuilder())
    private val multiCacheProvider = CacheProvider<SelectQuery, Collection<T>>(cacheBuilder())

    override suspend fun getIfPresent(where: String): T? {
        TODO("Not yet implemented")
    }

    override suspend fun getIfPresent(where: String, loader: suspend () -> T?): T? {
        return singleCacheProvider.get(where) {
            loader()
        }
    }

    override fun getIfPresent(select: SelectQuery): Flow<T> {
        TODO("Not yet implemented")
    }

    override fun getIfPresent(select: SelectQuery, loader: () -> Flow<T>): Flow<T> {
        return flow {
            emitAll(
                multiCacheProvider.get(select) {
                    loader().toList()
                }.asFlow()
            )
        }
    }

    override suspend fun clear() {
        singleCacheProvider.clear()
        multiCacheProvider.clear()
    }
}
