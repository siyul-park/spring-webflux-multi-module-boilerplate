package io.github.siyual_park.data.repository.cache

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNCHECKED_CAST")
class InMemoryStorage<T : Any, ID : Any>(
    cacheBuilder: CacheBuilder<ID, T>,
    private val idExtractor: Extractor<T, ID>
) : Storage<T, ID> {
    private val indexes = mutableMapOf<String, MutableMap<*, ID>>()
    private val extractors = mutableMapOf<String, Extractor<T, *>>()

    private val cache: Cache<ID, T> = cacheBuilder
        .removalListener<ID, T> {
            it.value?.let { entity ->
                indexes.forEach { (name, index) ->
                    val extractor = extractors[name] ?: return@forEach
                    index.remove(extractor.getKey(entity))
                }
            }
        }.build()

    override fun <KEY : Any> createIndex(name: String, extractor: Extractor<T, KEY>) {
        indexes[name] = ConcurrentHashMap<KEY, ID>()
        extractors[name] = extractor
    }

    override fun removeIndex(name: String) {
        indexes.remove(name)
        extractors.remove(name)
    }

    override fun getExtractors(): Map<String, Extractor<T, *>> {
        return extractors
    }

    override fun containsIndex(name: String): Boolean {
        return indexes.keys.contains(name)
    }

    override fun <KEY : Any> getIfPresent(key: KEY, index: String): T? {
        val indexMap = indexes[index] ?: return null
        val id = indexMap[key] ?: return null

        return getIfPresent(id)
    }

    override fun <KEY : Any> getIfPresent(key: KEY, index: String, loader: () -> T?): T? {
        val indexMap = getIndex(index)
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

    override suspend fun <KEY : Any> getIfPresentAsync(key: KEY, index: String, loader: suspend () -> T?): T? {
        val indexMap = getIndex(index)
        val id = indexMap[key]

        return if (id == null) {
            val entity = loader()
            if (entity == null) {
                null
            } else {
                idExtractor.getKey(entity)?.let {
                    this.getIfPresentAsync(it) { entity }
                }
            }
        } else {
            getIfPresentAsync(id, loader)
        }
    }

    override fun getIfPresent(id: ID): T? {
        return cache.getIfPresent(id)
    }

    override fun getIfPresent(id: ID, loader: () -> T?): T? {
        return cache.getIfPresent(id)
            ?: loader()?.also { put(it) }
    }

    override suspend fun getIfPresentAsync(id: ID, loader: suspend () -> T?): T? {
        return cache.getIfPresent(id)
            ?: loader()?.also { put(it) }
    }

    override fun remove(id: ID) {
        cache.invalidate(id)
    }

    override fun delete(entity: T) {
        idExtractor.getKey(entity)?.let { remove(it) }
    }

    override fun put(entity: T) {
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

    override fun clear() {
        cache.invalidateAll()
        indexes.forEach { (_, index) -> index.run { clear() } }
    }

    private fun getIndex(index: String): MutableMap<*, ID> {
        return indexes[index] ?: throw RuntimeException("Can't find index.")
    }
}
