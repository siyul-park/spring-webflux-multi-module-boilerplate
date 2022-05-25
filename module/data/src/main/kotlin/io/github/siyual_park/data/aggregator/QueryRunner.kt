package io.github.siyual_park.data.aggregator

import io.github.siyual_park.data.criteria.Criteria
import io.github.siyual_park.data.criteria.FilterCriteriaParser
import io.github.siyual_park.data.criteria.or
import io.github.siyual_park.data.repository.QueryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlin.reflect.KClass

class QueryRunner<T : Any>(
    private val repository: QueryRepository<T, *>,
    private val store: QueryStorage<T>,
    private val criteria: Criteria,
    clazz: KClass<T>,
) {
    private val parser = FilterCriteriaParser(clazz)

    init {
        store.add(criteria)
    }

    fun run(): Flow<T> {
        return flow {
            if (store.pop(criteria)?.let { emitAll(it.asFlow()) } != null) {
                return@flow
            }

            store.withLock {
                if (store.pop(criteria)?.let { emitAll(it.asFlow()) } != null) {
                    return@withLock
                }

                val free = store.free().toMutableSet().apply {
                    add(criteria)
                }
                val distributed = distribute(free)
                distributed.forEach { (key, value) ->
                    store.push(key, value)
                }

                distributed[criteria]?.let {
                    emitAll(it.asFlow())
                }
            }
        }
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

    fun clear() {
        store.remove(criteria)
    }
}
