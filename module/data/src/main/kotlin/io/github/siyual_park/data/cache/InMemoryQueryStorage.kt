package io.github.siyual_park.data.cache

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.criteria.RuntimeCriteriaParser
import kotlin.reflect.KClass

class InMemoryQueryStorage<T : Any>(
    clazz: KClass<T>,
    cacheBuilder: (() -> CacheBuilder<Any, Any>)
) : QueryStorage<T> {
    private val multiCache = CacheProvider<SelectQuery, Collection<T>>(cacheBuilder())
    private val runtimeCriteriaParser = RuntimeCriteriaParser(clazz)

    override suspend fun getIfPresent(select: SelectQuery): Collection<T>? {
        return multiCache.getIfPresent(select)
    }

    override suspend fun getIfPresent(select: SelectQuery, loader: suspend () -> Collection<T>?): Collection<T>? {
        return multiCache.getIfPresent(select, loader)
    }

    override suspend fun remove(select: SelectQuery) {
        multiCache.remove(select)
    }

    override suspend fun put(select: SelectQuery, value: Collection<T>) {
        multiCache.put(select, value)
    }

    override suspend fun clear(entity: T) {
        multiCache.entries().forEach { (key, _) ->
            if (key.where == null) {
                multiCache.remove(key)
            } else {
                val filter = runtimeCriteriaParser.parse(key.where) ?: { true }
                if (filter(entity)) {
                    multiCache.remove(key)
                }
            }
        }
    }

    override suspend fun clear() {
        multiCache.clear()
    }

    override suspend fun entries(): Set<Pair<SelectQuery, Collection<T>>> {
        return multiCache.entries()
    }
}
