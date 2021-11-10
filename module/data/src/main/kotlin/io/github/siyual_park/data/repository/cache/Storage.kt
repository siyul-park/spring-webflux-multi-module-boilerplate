package io.github.siyual_park.data.repository.cache

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.dao.EmptyResultDataAccessException
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNCHECKED_CAST")
class Storage<T : Any, ID : Any>(
    cacheBuilder: CacheBuilder<ID, T>,
    private val idExtractor: Extractor<T, ID>
) {
    private val indexes = mutableMapOf<String, MutableMap<*, ID>>()
    private val extractors = mutableMapOf<String, Extractor<T, *>>()

    val indexNames: Set<String>
        get() = indexes.keys

    private val cache: Cache<ID, T> = cacheBuilder
        .removalListener<ID, T> {
            val entity: T = it.value
            indexes.forEach { (name, index) ->
                val extractor = extractors[name] ?: return@forEach
                index.remove(extractor.getKey(entity))
            }
        }.build()

    fun <KEY : Any> createIndex(name: String, extractor: Extractor<T, KEY>) {
        indexes[name] = ConcurrentHashMap<KEY, ID>()
        extractors[name] = extractor
    }

    fun <KEY : Any> removeIndex(name: String) {
        indexes.remove(name)
        extractors.remove(name)
    }

    fun get(id: ID, loader: () -> T): T {
        return getIfPresent(id, loader) ?: throw EmptyResultDataAccessException(1)
    }

    fun <KEY : Any> getIfPresent(key: KEY, index: String): T? {
        val indexMap = indexes[index] ?: return null
        val id = indexMap[key] ?: return null

        return getIfPresent(id)
    }

    suspend fun <KEY : Any> getIfPresentAsync(key: KEY, index: String, loader: suspend () -> T?): T? {
        val indexMap = indexes[index] ?: throw RuntimeException("Can't find index.")
        val id = indexMap[key]

        return if (id == null) {
            val entity = loader()
            if (entity == null) {
                null
            } else {
                idExtractor.getKey(entity)?.let {
                    getIfPresent(it) { entity }
                }
            }
        } else {
            getIfPresentAsync(id, loader)
        }
    }

    fun <KEY : Any> getIfPresent(key: KEY, index: String, loader: () -> T?): T? {
        val indexMap = indexes[index] ?: throw RuntimeException("Can't find index.")
        val id = indexMap[key]

        return if (id == null) {
            val entity = loader()
            if (entity == null) {
                null
            } else {
                idExtractor.getKey(entity)?.let {
                    getIfPresent(it) { entity }
                }
            }
        } else {
            getIfPresent(id, loader)
        }
    }

    fun getIfPresent(id: ID): T? {
        return cache.getIfPresent(id)
    }

    suspend fun getIfPresentAsync(id: ID, loader: suspend () -> T?): T? {
        return cache.getIfPresent(id)
            ?: loader()?.also { put(it) }
    }

    fun getIfPresent(id: ID, loader: () -> T?): T? {
        return cache.getIfPresent(id)
            ?: loader()?.also { put(it) }
    }

    fun remove(entity: T) {
        idExtractor.getKey(entity)?.let { removeBy(it) }
    }

    fun <KEY : Any> removeBy(key: KEY, index: String) {
        val indexMap = indexes[index] ?: return
        val id = indexMap[key] ?: return

        removeBy(id)
    }

    fun removeBy(id: ID) {
        cache.invalidate(id)
    }

    fun put(entity: T) {
        val id = idExtractor.getKey(entity) ?: return
        cache.put(id, entity)

        indexes.forEach { (name, index) ->
            val extractor = extractors[name] ?: return@forEach
            val key = extractor.getKey(entity) ?: return@forEach

            index as MutableMap<Any, ID>
            extractor as Extractor<T, Any>

            index[key] = id
        }
    }

    fun clear() {
        cache.invalidateAll()
        indexes.forEach { (_, index) -> index.clear() }
    }
}
