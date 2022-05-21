package io.github.siyual_park.data.repository.cache

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList

class SimpleCachedQueryStorage<T : Any>(
    private val singleCacheProvider: CacheProvider<String, T?>,
    private val multiCacheProvider: CacheProvider<SelectQuery, Collection<T>>
) : QueryStorage<T> {

    override suspend fun getIfPresent(where: String): T? {
        return singleCacheProvider.getIfPresent(where)
    }

    override suspend fun getIfPresent(where: String, loader: suspend () -> T?): T? {
        return singleCacheProvider.get(where) {
            loader()
        }
    }

    override fun getIfPresent(select: SelectQuery): Flow<T> {
        return flow {
            multiCacheProvider.getIfPresent(select)?.let {
                emitAll(it.asFlow())
            }
        }
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
