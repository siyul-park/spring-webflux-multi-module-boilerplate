package io.github.siyual_park.data.cache

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.collect.Maps
import io.github.siyual_park.data.WeekProperty
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNCHECKED_CAST")
class InMemoryStorage<ID : Any, T : Any>(
    cacheBuilder: (() -> CacheBuilder<Any, Any>),
    private val id: WeekProperty<T, ID?>
) : Storage<ID, T> {
    private val indexes = Maps.newConcurrentMap<String, MutableMap<*, ID>>()
    private val properties = Maps.newConcurrentMap<String, WeekProperty<T, *>>()
    private val mutexes = Collections.synchronizedMap(WeakHashMap<ID, Mutex>())

    private val cache: Cache<ID, T> = cacheBuilder()
        .removalListener<ID, T> {
            it.value?.let { entity ->
                indexes.forEach { (name, index) ->
                    val extractor = properties[name] ?: return@forEach
                    index.remove(extractor.get(entity))
                }
            }
            it.key?.let { id ->
                mutexes.remove(id)
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
        return indexes.keys.contains(name)
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY): T? {
        val indexMap = indexes[index] ?: return null
        val id = indexMap[key] ?: return null

        return getIfPresent(id)
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY, loader: suspend () -> T?): T? {
        val indexMap = getIndex(index)
        val id = indexMap[key]

        return if (id == null) {
            val entity = loader()
            if (entity == null) {
                null
            } else {
                this.id.get(entity)?.let {
                    this.getIfPresent(it) { entity }
                }
            }
        } else {
            getIfPresent(id, loader)
        }
    }

    override suspend fun getIfPresent(id: ID): T? {
        return cache.getIfPresent(id)
    }

    override suspend fun getIfPresent(id: ID, loader: suspend () -> T?): T? {
        val existed = cache.getIfPresent(id)
        if (existed != null) {
            return existed
        }

        val mutex = mutexes.getOrPut(id) { Mutex() }
        return mutex.withLock {
            cache.getIfPresent(id)
                ?: loader()?.also { add(it) }
        }
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

    private fun getIndex(index: String): MutableMap<*, ID> {
        return indexes[index] ?: throw RuntimeException("Can't find index.")
    }
}
