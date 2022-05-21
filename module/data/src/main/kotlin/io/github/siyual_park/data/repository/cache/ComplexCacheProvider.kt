package io.github.siyual_park.data.repository.cache

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Collections
import java.util.WeakHashMap

class ComplexCacheProvider<K : Any, T : Any?>(
    cacheBuilder: CacheBuilder<Any, Any>,
) : CacheProvider<K, T> {
    private val mutexes = Collections.synchronizedMap(WeakHashMap<K, Mutex>())
    private val cache: Cache<K, T> = cacheBuilder
        .removalListener<K, T> {
            it.key?.let { id ->
                mutexes.remove(id)
            }
        }.build()

    override suspend fun get(key: K, value: suspend () -> T): T {
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

    override suspend fun getIfPresent(key: K): T? {
        return cache.getIfPresent(key)
    }

    override suspend fun getIfPresent(key: K, value: suspend () -> T?): T? {
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

    override suspend fun put(key: K, value: T) {
        if (value != null) {
            cache.put(key, value)
        }
    }

    override suspend fun remove(key: K) {
        cache.invalidate(key)
    }

    override suspend fun entries(): Set<Pair<K, T>> {
        return emptySet()
    }

    override suspend fun clear() {
        cache.invalidateAll()
        mutexes.clear()
    }
}
