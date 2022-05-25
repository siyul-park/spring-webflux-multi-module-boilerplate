package io.github.siyual_park.data.aggregator

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.criteria.Criteria
import io.github.siyual_park.data.repository.QueryRepository
import org.apache.commons.collections4.map.AbstractReferenceMap
import org.apache.commons.collections4.map.ReferenceMap
import java.util.Collections
import kotlin.reflect.KClass

class QueryAggregator<T : Any, ID : Any>(
    private val repository: QueryRepository<T, ID>,
    private val clazz: KClass<T>,
    private val cacheBuilder: () -> CacheBuilder<Any, Any>,
) {
    private val stores = Collections.synchronizedMap(
        ReferenceMap<String, QueryStorage<T>>(
            AbstractReferenceMap.ReferenceStrength.HARD, AbstractReferenceMap.ReferenceStrength.WEAK
        )
    )

    fun runner(name: String, criteria: Criteria): QueryRunner<T, ID> {
        val store = stores.getOrPut(name) { QueryStorage(cacheBuilder) }
        return QueryRunner(repository, store, criteria, clazz)
    }

    fun clear() {
        stores.values.toList().forEach { it.clear() }
        stores.clear()
    }
}
