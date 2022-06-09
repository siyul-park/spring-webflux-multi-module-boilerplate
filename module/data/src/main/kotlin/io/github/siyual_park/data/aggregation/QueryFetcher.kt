package io.github.siyual_park.data.aggregation

import io.github.siyual_park.data.cache.SelectQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.springframework.dao.EmptyResultDataAccessException

class QueryFetcher<T : Any>(
    private val query: SelectQuery,
    private val queryAggregator: QueryAggregator<T>
) {
    suspend fun clear() {
        queryAggregator.clear(query)
    }

    suspend fun fetchOne(): T? {
        return fetch().toList().firstOrNull()
    }

    fun fetch(): Flow<T> {
        return queryAggregator.fetch(query)
    }
}

suspend fun <T : Any> QueryFetcher<T>.fetchOneOrFail(): T {
    return fetchOne() ?: throw EmptyResultDataAccessException(1)
}
