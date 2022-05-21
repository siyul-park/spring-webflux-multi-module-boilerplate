package io.github.siyual_park.data.repository.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.WeakHashMap

class SimpleCacheProvider<K : Any, T : Any?> : CacheProvider<K, T> {
    private val cache = WeakHashMap<K, T>()

    override suspend fun get(key: K, value: suspend () -> T): T {
        return cache[key] ?: run {
            val exists = cache[key]
            if (exists != null) {
                exists
            } else {
                val newone = value()
                if (newone != null) {
                    cache.put(key, newone)
                }
                newone
            }
        }
    }

    override suspend fun getIfPresent(key: K): T? {
        return cache[key]
    }

    override suspend fun getIfPresent(key: K, value: suspend () -> T?): T? {
        return cache[key]
    }

    override suspend fun clear() {
        cache.clear()
    }
}
