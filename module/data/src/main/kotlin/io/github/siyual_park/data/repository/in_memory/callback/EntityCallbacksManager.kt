package io.github.siyual_park.data.repository.in_memory.callback

class EntityCallbacksManager<T> : EntityCallbacks<T> {
    private var callbacks = mutableListOf<EntityCallbacks<T>>()

    fun register(callback: EntityCallbacks<T>) {
        callbacks.add(callback)
    }

    override fun onCreate(entity: T) {
        callbacks.forEach { it.onCreate(entity) }
    }

    override fun onDelete(entity: T) {
        callbacks.forEach { it.onDelete(entity) }
    }

    override fun onUpdate(origin: T, entity: T) {
        callbacks.forEach { it.onUpdate(origin, entity) }
    }
}
