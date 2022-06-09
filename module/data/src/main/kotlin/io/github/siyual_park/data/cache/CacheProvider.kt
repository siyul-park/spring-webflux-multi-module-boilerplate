package io.github.siyual_park.data.cache

import com.google.common.cache.CacheBuilder

class CacheProvider<K : Any, T : Any>(
    cacheBuilder: CacheBuilder<Any, Any>,
) {
    private val cache = cacheBuilder.build<K, T>()

    private var updated = false

    suspend fun get(key: K, value: suspend () -> T): T {
        return getIfPresent(key, value)!!
    }

    suspend fun getIfPresent(key: K): T? {
        return cache.getIfPresent(key)
    }

    suspend fun getIfPresent(key: K, value: suspend () -> T?): T? {
        return cache.getIfPresent(key) ?: run {
            val newone = value()
            if (newone != null) {
                updated = true
                cache.put(key, newone)
            }
            newone
        }
    }

    suspend fun put(key: K, value: T) {
        updated = true
        cache.put(key, value)
    }

    suspend fun remove(key: K) {
        cache.invalidate(key)
    }

    suspend fun entries(): Set<Pair<K, T>> {
        return cache.asMap().entries.map { it.key to it.value }.toSet()
    }

    suspend fun clear() {
        if (updated) {
            updated = false
            cache.invalidateAll()
        }
    }
}
