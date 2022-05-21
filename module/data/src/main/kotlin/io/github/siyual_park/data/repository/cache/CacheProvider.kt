package io.github.siyual_park.data.repository.cache

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.collect.Maps
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CacheProvider<K : Any, T : Any?>(
    cacheBuilder: CacheBuilder<Any, Any>,
) {
    private val mutexes = Maps.newConcurrentMap<K, Mutex>()
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

    suspend fun clear() {
        cache.invalidateAll()
        mutexes.clear()
    }
}
