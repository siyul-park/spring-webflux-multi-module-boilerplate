package io.github.siyual_park.data.repository.cache

import io.github.siyual_park.data.repository.Extractor

interface Storage<T : Any, ID : Any> {
    val idExtractor: Extractor<T, ID>

    fun <KEY : Any> createIndex(name: String, extractor: Extractor<T, KEY>)
    fun removeIndex(name: String)
    fun containsIndex(name: String): Boolean

    fun getExtractors(): Map<String, Extractor<T, *>>

    suspend fun <KEY : Any> getIfPresent(index: String, key: KEY): T?
    suspend fun <KEY : Any> getIfPresent(index: String, key: KEY, loader: suspend () -> T?): T?

    suspend fun getIfPresent(id: ID): T?
    suspend fun getIfPresent(id: ID, loader: suspend () -> T?): T?

    suspend fun remove(id: ID)

    suspend fun delete(entity: T)
    suspend fun put(entity: T)

    suspend fun clear()
}
