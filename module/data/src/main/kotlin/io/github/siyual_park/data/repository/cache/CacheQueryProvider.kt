package io.github.siyual_park.data.repository.cache

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.cache.CacheProvider

class CacheQueryProvider<T : Any>(
    cacheBuilder: (() -> CacheBuilder<Any, Any>)
) {
    private val singleCache = CacheProvider<String, T>(cacheBuilder())
    private val multiCache = CacheProvider<SelectQuery, Collection<T>>(cacheBuilder())

    fun single() = singleCache
    fun multi() = multiCache
}
