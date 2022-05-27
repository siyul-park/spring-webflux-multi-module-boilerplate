package io.github.siyual_park.data.aggregation

import io.github.siyual_park.data.cache.QueryStorage
import io.github.siyual_park.data.criteria.Criteria
import io.github.siyual_park.data.repository.QueryRepository
import kotlinx.coroutines.sync.Mutex
import kotlin.reflect.KClass

class FetchContext<T : Any>(
    private val store: QueryStorage<T>,
    private val repository: QueryRepository<T, *>,
    private val clazz: KClass<T>,
) {
    private val links = CriteriaStore<Criteria>()
    private val mutex = Mutex()

    suspend fun clear() {
        links.clear()
        store.clear()
    }

    fun join(criteria: Criteria): CriteriaFetcher<T> {
        links.push(criteria)
        return CriteriaFetcher(criteria, links, store, repository, clazz, mutex)
    }
}
