package io.github.siyual_park.data.repository.cache

import kotlinx.coroutines.flow.Flow

interface QueryStorage<T : Any> {
    suspend fun getIfPresent(where: String): T?
    suspend fun getIfPresent(where: String, loader: suspend () -> T?): T?

    fun getIfPresent(select: SelectQuery, loader: () -> Flow<T>): Flow<T>
    fun getIfPresent(select: SelectQuery): Flow<T>

    suspend fun clear()
}
