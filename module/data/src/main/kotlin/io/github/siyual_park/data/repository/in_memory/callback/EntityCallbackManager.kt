package io.github.siyual_park.data.repository.in_memory.callback

class EntityCallbackManager<T> : EntityCallbacks<T> {
    private var createEntityCallback: CreateEntityCallback<T>? = null
    private var updateEntityCallback: UpdateEntityCallback<T>? = null
    private var deleteEntityCallback: DeleteEntityCallback<T>? = null

    fun register(callback: CreateEntityCallback<T>?) {
        createEntityCallback = callback
    }
    fun register(callback: UpdateEntityCallback<T>?) {
        updateEntityCallback = callback
    }
    fun register(callback: DeleteEntityCallback<T>?) {
        deleteEntityCallback = callback
    }

    override fun onCreate(entity: T) {
        createEntityCallback?.onCreate(entity)
    }

    override fun onDelete(entity: T) {
        deleteEntityCallback?.onDelete(entity)
    }

    override fun onUpdate(origin: T, entity: T) {
        updateEntityCallback?.onUpdate(origin, entity)
    }
}
