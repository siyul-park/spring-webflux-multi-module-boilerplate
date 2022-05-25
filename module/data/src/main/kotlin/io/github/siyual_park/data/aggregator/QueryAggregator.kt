package io.github.siyual_park.data.aggregator

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.criteria.Criteria
import io.github.siyual_park.data.criteria.FilterCriteriaParser
import io.github.siyual_park.data.criteria.or
import io.github.siyual_park.data.repository.QueryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Collections
import java.util.WeakHashMap
import kotlin.reflect.KClass

class QueryAggregator<T : Any, ID : Any>(
    private val repository: QueryRepository<T, ID>,
    clazz: KClass<T>,
    cacheBuilder: () -> CacheBuilder<Any, Any>,
) {
    private val data = cacheBuilder()
        .weakKeys()
        .build<Criteria, Collection<T>>()
    private val queries = Collections.newSetFromMap(WeakHashMap<Criteria, Boolean>())

    private val parser = FilterCriteriaParser(clazz)

    private val mutex = Mutex()

    fun register(criteria: Criteria) {
        queries.add(criteria)
    }

    fun run(criteria: Criteria): Flow<T> {
        return flow {
            if (data.getIfPresent(criteria)?.let { emitAll(it.asFlow()) } != null) {
                return@flow
            }

            mutex.withLock {
                if (data.getIfPresent(criteria)?.let { emitAll(it.asFlow()) } != null) {
                    return@withLock
                }

                val free = free().toMutableSet().apply {
                    add(criteria)
                }
                val distributed = distribute(free)
                distributed.forEach { (key, value) ->
                    data.put(key, value)
                }

                distributed[criteria]?.let {
                    emitAll(it.asFlow())
                }
            }
        }
    }

    private fun free(): Set<Criteria> {
        val existed = data.asMap().keys
        return queries.filter { !existed.contains(it) }.toSet()
    }

    private suspend fun distribute(queries: Collection<Criteria>): Map<Criteria, List<T>> {
        val aggregated = aggregate(queries)
        val filters = queries.associateWith { parser.parse(it) }
        val distributed = mutableMapOf<Criteria, MutableList<T>>()

        val result = repository.findAll(aggregated).toList()
        result.forEach { value ->
            filters.forEach { (criteria, filter) ->
                if (filter != null && filter(value)) {
                    distributed.getOrPut(criteria) { mutableListOf() }
                        .add(value)
                }
            }
        }

        return distributed
    }

    private fun aggregate(criterias: Collection<Criteria>): Criteria {
        return criterias.reduce { acc, cur -> acc.or(cur) }
    }
}
