package io.github.siyual_park.data.cache

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.collect.Maps
import io.github.siyual_park.data.WeekProperty
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNCHECKED_CAST")
class InMemoryStorage<ID : Any, T : Any>(
    cacheBuilder: (() -> CacheBuilder<Any, Any>),
    private val id: WeekProperty<T, ID?>
) : Storage<ID, T> {
    private val indexes = Maps.newConcurrentMap<String, MutableMap<*, ID>>()
    private val properties = Maps.newConcurrentMap<String, WeekProperty<T, *>>()

    private val cache: Cache<ID, T> = cacheBuilder()
        .removalListener<ID, T> {
            it.value?.let { entity ->
                indexes.forEach { (name, index) ->
                    val extractor = properties[name] ?: return@forEach
                    index.remove(extractor.get(entity))
                }
            }
        }.build()

    override suspend fun <KEY : Any> createIndex(name: String, property: WeekProperty<T, KEY>) {
        indexes[name] = ConcurrentHashMap<KEY, ID>()
        properties[name] = property
    }

    override suspend fun removeIndex(name: String) {
        indexes.remove(name)
        properties.remove(name)
    }

    override suspend fun getIndexes(): Map<String, WeekProperty<T, *>> {
        return properties
    }

    override suspend fun containsIndex(name: String): Boolean {
        return properties.contains(name)
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY): T? {
        val indexMap = indexes[index] ?: return null
        val id = indexMap[key] ?: return null

        return getIfPresent(id)
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY, loader: suspend () -> T?): T? {
        return getIfPresent(index, key) ?: loader()?.also { add(it) }
    }

    override suspend fun getIfPresent(id: ID): T? {
        return cache.getIfPresent(id)
    }

    override suspend fun getIfPresent(id: ID, loader: suspend () -> T?): T? {
        return cache.getIfPresent(id) ?: loader()?.also { add(it) }
    }

    override suspend fun remove(id: ID) {
        cache.invalidate(id)
    }

    override suspend fun add(entity: T) {
        val id = id.get(entity) ?: return
        cache.put(id, entity)

        indexes.forEach { (name, index) ->
            val property = properties[name] ?: return@forEach
            val key = property.get(entity) ?: return@forEach
            index as MutableMap<Any, ID>
            index[key] = id
        }
    }

    override suspend fun entries(): Set<Pair<ID, T>> {
        return cache.asMap().entries.map { it.key to it.value }.toSet()
    }

    override suspend fun clear() {
        cache.invalidateAll()
        indexes.forEach { (_, index) -> index.run { clear() } }
    }
}
