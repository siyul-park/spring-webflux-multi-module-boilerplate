package io.github.siyual_park.data.repository.cache

interface NestedStorage<T : Any, ID : Any> : Storage<T, ID> {
    val parent: NestedStorage<T, ID>?

    fun diff(): Pair<Set<T>, Set<ID>>

    fun fork(): NestedStorage<T, ID>
    fun join(storage: NestedStorage<T, ID>)
}
