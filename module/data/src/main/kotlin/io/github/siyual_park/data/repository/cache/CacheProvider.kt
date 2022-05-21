package io.github.siyual_park.data.repository.cache

interface CacheProvider<K : Any, T : Any?> {
    suspend fun get(key: K, value: suspend () -> T): T
    suspend fun getIfPresent(key: K): T?
    suspend fun getIfPresent(key: K, value: suspend () -> T?): T?
    suspend fun clear()
}
