package io.github.siyual_park.data.cache

interface NestedStorageFactory<ID : Any, T : Any> {
    suspend fun create(parent: NestedStorage<ID, T>? = null): NestedStorage<ID, T>
}
