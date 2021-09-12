package io.github.siyual_park.data.repository.in_memory.callback

interface CreateEntityCallback<T> {
    fun onCreate(entity: T)
}
