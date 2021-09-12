package io.github.siyual_park.data.repository.in_memory.callback

interface UpdateEntityCallback<T> {
    fun onUpdate(origin: T, entity: T)
}
