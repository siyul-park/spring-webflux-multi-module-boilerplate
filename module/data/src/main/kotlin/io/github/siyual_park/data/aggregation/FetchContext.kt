package io.github.siyual_park.data.aggregation

import io.github.siyual_park.data.cache.QueryStorage
import io.github.siyual_park.data.cache.ReferenceStore
import io.github.siyual_park.data.cache.SelectQuery
import io.github.siyual_park.data.criteria.Criteria
import io.github.siyual_park.data.repository.QueryRepository
import kotlinx.coroutines.sync.Mutex
import kotlin.reflect.KClass

class FetchContext<T : Any>(
    private val store: QueryStorage<T>,
    private val repository: QueryRepository<T, *>,
    private val clazz: KClass<T>,
) {
    private val links = ReferenceStore<SelectQuery>()
    private val mutex = Mutex()

    suspend fun clear() {
        links.clear()
        store.clear()
    }

    fun join(criteria: Criteria?, limit: Int? = null): QueryFetcher<T> {
        val query = SelectQuery(criteria, limit)
        links.push(query)
        return QueryFetcher(query, links, store, repository, clazz, mutex)
    }
}
