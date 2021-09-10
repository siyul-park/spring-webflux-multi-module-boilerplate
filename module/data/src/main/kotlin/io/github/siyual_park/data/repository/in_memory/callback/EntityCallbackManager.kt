package io.github.siyual_park.data.repository.in_memory.callback

class EntityCallbackManager<T> : EntityCallbacks<T> {
    var createEntityCallback: CreateEntityCallback<T>? = null
    var updateEntityCallback: UpdateEntityCallback<T>? = null
    var deleteEntityCallback: DeleteEntityCallback<T>? = null

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
