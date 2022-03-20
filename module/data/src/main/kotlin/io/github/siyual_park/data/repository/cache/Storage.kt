package io.github.siyual_park.data.repository.cache

interface Storage<T : Any, ID : Any> {
    fun <KEY : Any> createIndex(name: String, extractor: Extractor<T, KEY>)
    fun removeIndex(name: String)
    fun containsIndex(name: String): Boolean

    fun getExtractors(): Map<String, Extractor<T, *>>

    fun <KEY : Any> getIfPresent(key: KEY, index: String): T?
    fun <KEY : Any> getIfPresent(key: KEY, index: String, loader: () -> T?): T?
    suspend fun <KEY : Any> getIfPresentAsync(key: KEY, index: String, loader: suspend () -> T?): T?

    fun getIfPresent(id: ID): T?
    fun getIfPresent(id: ID, loader: () -> T?): T?
    suspend fun getIfPresentAsync(id: ID, loader: suspend () -> T?): T?

    fun remove(id: ID)

    fun delete(entity: T)
    fun put(entity: T)

    fun clear()
}
