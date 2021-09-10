package io.github.siyual_park.data.repository.in_memory.callback

interface DeleteEntityCallback<T> {
    fun onDelete(entity: T)
}
