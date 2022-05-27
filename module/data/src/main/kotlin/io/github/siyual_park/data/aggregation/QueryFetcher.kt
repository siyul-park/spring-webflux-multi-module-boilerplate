package io.github.siyual_park.data.aggregation

import io.github.siyual_park.data.cache.QueryStorage
import io.github.siyual_park.data.cache.ReferenceStore
import io.github.siyual_park.data.cache.SelectQuery
import io.github.siyual_park.data.criteria.Criteria
import io.github.siyual_park.data.criteria.RuntimeCriteriaParser
import io.github.siyual_park.data.criteria.or
import io.github.siyual_park.data.repository.QueryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.dao.EmptyResultDataAccessException
import kotlin.reflect.KClass

class QueryFetcher<T : Any>(
    private val query: SelectQuery,
    private val links: ReferenceStore<SelectQuery>,
    private val store: QueryStorage<T>,
    private val repository: QueryRepository<T, *>,
    clazz: KClass<T>,
    private val mutex: Mutex = Mutex()
) {
    private val parser = RuntimeCriteriaParser(clazz)

    suspend fun fetchOne(): T {
        return fetchOneOrNull() ?: throw EmptyResultDataAccessException(1)
    }

    suspend fun fetchOneOrNull(): T? {
        return fetch().firstOrNull()
    }

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
                    val merged = mutableSetOf<SelectQuery>().also {
                        it.addAll(free)
                        it.add(query)
                    }

                    val limit = free.map { it.limit }.fold(query.limit) { acc, cur ->
                        if (cur == null || acc == null) {
                            null
                        } else {
                            acc + cur
                        }
                    }

                    val result = repository.findAll(
                        merge(merged.mapNotNull { it.where }.toSet()),
                        limit = limit
                    ).toList()
                    val distributed = distribute(merged, result)

                    distributed.forEach { (key, value) ->
                        if (key != query) {
                            store.put(key, value)
                        } else {
                            emitAll(value.asFlow())
                        }
                    }
                }
            }
        }
    }

    private suspend fun free(): Set<SelectQuery> {
        val curr = links.entries().toMutableSet()
        val used = store.entries().map { it.first }

        used.forEach {
            curr.remove(it)
        }

        return curr
    }

    private fun merge(criteria: Set<Criteria>): Criteria {
        return criteria.reduce { acc, cur -> cur.or(acc) }
    }

    private fun distribute(criteria: Set<SelectQuery>, values: List<T>): Map<SelectQuery, List<T>> {
        val result = mutableMapOf<SelectQuery, MutableList<T>>()

        criteria.forEach { q ->
            val filter = q.where?.let { parser.parse(it) } ?: { false }
            values.forEach { value ->
                if (filter(value)) {
                    result.getOrPut(q) { mutableListOf() }.add(value)
                }
            }
        }

        return result
    }
}
