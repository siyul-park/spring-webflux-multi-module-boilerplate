package io.github.siyual_park.data.repository.cache

interface StorageManager<T : Any, ID : Any> {
    val root: Storage<T, ID>

    suspend fun getCurrent(): Storage<T, ID>
}
