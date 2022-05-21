package io.github.siyual_park.data.repository.cache

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Collections
import java.util.WeakHashMap

class CacheProvider<K : Any, T : Any?>(
    cacheBuilder: CacheBuilder<Any, Any>,
) {
    private val mutexes = Collections.synchronizedMap(WeakHashMap<K, Mutex>())
    private val cache: Cache<K, T> = cacheBuilder
        .removalListener<K, T> {
            it.key?.let { id ->
                mutexes.remove(id)
            }
        }.build()

    suspend fun get(key: K, value: suspend () -> T): T {
        return cache.getIfPresent(key) ?: run {
            val mutex = mutexes.getOrPut(key) { Mutex() }
            mutex.withLock {
                val exists = cache.getIfPresent(key)
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
    }

    suspend fun getIfPresent(key: K): T? {
        return cache.getIfPresent(key)
    }

    suspend fun getIfPresent(key: K, value: suspend () -> T?): T? {
        return cache.getIfPresent(key) ?: run {
            val mutex = mutexes.getOrPut(key) { Mutex() }
            mutex.withLock {
                val exists = cache.getIfPresent(key)
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
    }

    suspend fun put(key: K, value: T) {
        if (value != null) {
            cache.put(key, value)
        }
    }

    suspend fun remove(key: K) {
        cache.invalidate(key)
    }

    suspend fun entries(): Set<Pair<K, T>> {
        return cache.asMap().entries.map { it.key to it.value }.toSet()
    }

    suspend fun clear() {
        cache.invalidateAll()
        mutexes.clear()
    }
}
