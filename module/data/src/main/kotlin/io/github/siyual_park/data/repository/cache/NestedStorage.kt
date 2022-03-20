package io.github.siyual_park.data.repository.cache

interface NestedStorage<T : Any, ID : Any> : Storage<T, ID> {
    val parent: NestedStorage<T, ID>?

    fun getCreated(): Set<T>
    fun getRemoved(): Set<ID>

    fun fork(): NestedStorage<T, ID>
    fun join(storage: NestedStorage<T, ID>)
}

fun <T : Any, ID : Any> NestedStorage<T, ID>.root(): NestedStorage<T, ID>? {
    var parent = parent
    var current = this

    while (parent != null) {
        val tmp = parent
        parent = parent.parent
        current = tmp
    }

    return current
}
