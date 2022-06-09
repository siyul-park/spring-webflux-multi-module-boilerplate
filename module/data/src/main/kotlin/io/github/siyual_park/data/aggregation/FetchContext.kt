package io.github.siyual_park.data.aggregation

import io.github.siyual_park.data.cache.SelectQuery
import io.github.siyual_park.data.criteria.Criteria
import io.github.siyual_park.data.repository.QueryRepository
import kotlin.reflect.KClass

class FetchContext<T : Any>(
    repository: QueryRepository<T, *>,
    clazz: KClass<T>,
) {
    private val queryAggregator = QueryAggregator(repository, clazz)

    suspend fun clear() {
        queryAggregator.clear()
    }

    suspend fun clear(entity: T) {
        queryAggregator.clear(entity)
    }

    fun join(criteria: Criteria?, limit: Int? = null): QueryFetcher<T> {
        val query = SelectQuery(criteria, limit)
        queryAggregator.link(query)
        return QueryFetcher(query, queryAggregator)
    }
}
