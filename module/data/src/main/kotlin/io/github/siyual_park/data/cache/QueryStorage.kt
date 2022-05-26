package io.github.siyual_park.data.cache

import io.github.siyual_park.data.criteria.Criteria
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList

interface QueryStorage<T : Any> {
    suspend fun getIfPresent(select: SelectQuery): Collection<T>?
    suspend fun getIfPresent(select: SelectQuery, loader: suspend () -> Collection<T>?): Collection<T>?

    suspend fun remove(select: SelectQuery)
    suspend fun put(select: SelectQuery, value: Collection<T>)

    suspend fun clear(entity: T)
    suspend fun clear()

    suspend fun entries(): Set<Pair<SelectQuery, Collection<T>>>
}

fun <T : Any> QueryStorage<T>.get(select: SelectQuery, loader: () -> Flow<T>): Flow<T> {
    return flow {
        getIfPresent(select) { loader().toList() }?.let { emitAll(it.asFlow()) }
    }
}

suspend fun <T : Any> QueryStorage<T>.getIfPresent(where: Criteria): T? {
    val query = SelectQuery(where, limit = 0, offset = 0)
    return getIfPresent(query)?.firstOrNull()
}
suspend fun <T : Any> QueryStorage<T>.getIfPresent(where: Criteria, loader: suspend () -> T?): T? {
    val query = SelectQuery(where, limit = 0, offset = 0)
    return getIfPresent(query) {
        loader()?.let { listOf(it) }
    }?.firstOrNull()
}
