package io.github.siyual_park.data.cache

interface GeneralNestedStorage<S : GeneralNestedStorage<S>> {
    val parent: S?

    suspend fun fork(): S
    suspend fun merge(storage: S)
    suspend fun clear()
}
