package io.github.siyual_park.data.repository.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Collections
import java.util.WeakHashMap

class SimpleCacheProvider<K : Any, T : Any?> : CacheProvider<K, T> {
    private val cache = WeakHashMap<K, T>()
    private val mutexes = Collections.synchronizedMap(WeakHashMap<K, Mutex>())

    override suspend fun get(key: K, value: suspend () -> T): T {
        return cache[key] ?: run {
            val mutex = mutexes.getOrPut(key) { Mutex() }
            mutex.withLock {
                val exists = cache[key]
                if (exists != null) {
                    exists
                } else {
                    val newone = value()
                    if (newone != null) {
                        cache[key] = newone
                    }
                    newone
                }
            }
        }
    }

    override suspend fun getIfPresent(key: K): T? {
        return cache[key]
    }

    override suspend fun getIfPresent(key: K, value: suspend () -> T?): T? {
        return cache[key] ?: run {
            val mutex = mutexes.getOrPut(key) { Mutex() }
            mutex.withLock {
                val exists = cache[key]
                if (exists != null) {
                    exists
                } else {
                    val newone = value()
                    if (newone != null) {
                        cache[key] = newone
                    }
                    newone
                }
            }
        }
    }

    override suspend fun put(key: K, value: T) {
        if (value != null) {
            cache[key] = value
        }
    }

    override suspend fun remove(key: K) {
        cache.remove(key)
        mutexes.remove(key)
    }

    override suspend fun clear() {
        cache.clear()
        mutexes.clear()
    }

    override suspend fun entries(): Set<Pair<K, T>> {
        return cache.entries.map { it.key to it.value }.toSet()
    }
}
