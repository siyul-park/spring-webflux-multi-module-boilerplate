package io.github.siyual_park.data.aggregator

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.criteria.Criteria
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Collections
import java.util.WeakHashMap

class QueryStorage<T : Any>(
    cacheBuilder: () -> CacheBuilder<Any, Any>,
) {
    private val queries = Collections.newSetFromMap(WeakHashMap<Criteria, Boolean>())
    private val data = cacheBuilder()
        .weakKeys()
        .build<Criteria, Collection<T>>()

    private val mutex = Mutex()

    fun add(criteria: Criteria) {
        queries.add(criteria)
    }
    fun remove(criteria: Criteria) {
        queries.remove(criteria)
    }

    fun pop(criteria: Criteria): Collection<T>? {
        if (!queries.contains(criteria)) {
            return null
        }
        return data.getIfPresent(criteria).also {
            data.invalidate(criteria)
        }
    }

    fun push(criteria: Criteria, value: Collection<T>) {
        if (!queries.contains(criteria)) {
            return
        }
        return data.put(criteria, value)
    }

    fun free(): Set<Criteria> {
        val existed = data.asMap().keys
        return queries.filter { !existed.contains(it) }.toSet()
    }

    fun clear() {
        queries.clear()
        data.invalidateAll()
    }

    suspend fun <T> withLock(func: suspend () -> T): T {
        return mutex.withLock {
            func()
        }
    }
}
