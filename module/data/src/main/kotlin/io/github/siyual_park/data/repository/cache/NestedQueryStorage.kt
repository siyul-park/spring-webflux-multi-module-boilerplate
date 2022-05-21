package io.github.siyual_park.data.repository.cache

interface NestedQueryStorage<T : Any> : QueryStorage<T> {
    val parent: NestedQueryStorage<T>?
    suspend fun fork(): NestedQueryStorage<T>
}
