package io.github.siyual_park.data.repository.in_memory.callback

interface EntityCallbacks<T> : CreateEntityCallback<T>, UpdateEntityCallback<T>, DeleteEntityCallback<T>
