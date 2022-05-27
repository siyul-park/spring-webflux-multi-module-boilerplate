package io.github.siyual_park.data.aggregation

import io.github.siyual_park.data.cache.QueryStorage
import io.github.siyual_park.data.cache.SelectQuery
import io.github.siyual_park.data.criteria.Criteria
import io.github.siyual_park.data.criteria.RuntimeCriteriaParser
import io.github.siyual_park.data.criteria.or
import io.github.siyual_park.data.repository.QueryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass

class CriteriaFetcher<T : Any>(
    private val criteria: Criteria,
    private val links: CriteriaStore<Criteria>,
    private val store: QueryStorage<T>,
    private val repository: QueryRepository<T, *>,
    clazz: KClass<T>,
    private val mutex: Mutex = Mutex()
) {
    private val query = SelectQuery(criteria)
    private val parser = RuntimeCriteriaParser(clazz)

    fun fetch(): Flow<T> {
        return flow {
            store.getIfPresent(query)?.let {
                store.remove(query)
                emitAll(it.asFlow())
            } ?: mutex.withLock {
                store.getIfPresent(query)?.let {
                    store.remove(query)
                    emitAll(it.asFlow())
                } ?: run {
                    val free = free()
                    val merged = mutableSetOf<Criteria>().also {
                        it.addAll(free)
                        it.add(criteria)
                    }

                    val result = repository.findAll(merge(merged)).toList()
                    val distributed = distribute(merged, result)

                    distributed.forEach { (key, value) ->
                        if (key != criteria) {
                            store.put(SelectQuery(key), value)
                        } else {
                            emitAll(value.asFlow())
                        }
                    }
                }
            }
        }
    }

    private suspend fun free(): Set<Criteria> {
        val curr = links.entries().toMutableSet()
        val used = store.entries().mapNotNull { it.first.where }

        used.forEach {
            curr.remove(it)
        }

        return curr
    }

    private fun merge(criteria: Set<Criteria>): Criteria {
        return criteria.reduce { acc, cur -> cur.or(acc) }
    }

    private fun distribute(criteria: Set<Criteria>, values: List<T>): Map<Criteria, List<T>> {
        val result = mutableMapOf<Criteria, MutableList<T>>()

        criteria.forEach { c ->
            val filter = parser.parse(c) ?: { false }
            values.forEach { value ->
                if (filter(value)) {
                    result.getOrPut(c) { mutableListOf() }
                        .let { it.add(value) }
                }
            }
        }

        return result
    }
}
