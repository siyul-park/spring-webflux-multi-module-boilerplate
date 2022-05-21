package io.github.siyual_park.data.cache

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList

interface QueryStorage<T : Any> {
    suspend fun getIfPresent(where: String): T?
    suspend fun getIfPresent(where: String, loader: suspend () -> T?): T?

    suspend fun getIfPresent(select: SelectQuery): Collection<T>?
    suspend fun getIfPresent(select: SelectQuery, loader: suspend () -> Collection<T>?): Collection<T>?

    suspend fun remove(where: String)
    suspend fun remove(select: SelectQuery)

    suspend fun put(where: String, value: T)
    suspend fun put(select: SelectQuery, value: Collection<T>)

    suspend fun clear()
    suspend fun entries(): Pair<Set<Pair<String, T>>, Set<Pair<SelectQuery, Collection<T>>>>
}

fun <T : Any> QueryStorage<T>.get(select: SelectQuery, loader: () -> Flow<T>): Flow<T> {
    return flow {
        getIfPresent(select) { loader().toList() }?.let { emitAll(it.asFlow()) }
    }
}