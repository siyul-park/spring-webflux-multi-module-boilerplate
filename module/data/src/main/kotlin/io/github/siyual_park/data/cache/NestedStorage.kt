package io.github.siyual_park.data.cache

interface NestedStorage<ID : Any, T : Any> : Storage<ID, T>, GeneralNestedStorage<NestedStorage<ID, T>> {
    suspend fun checkout(): Map<ID, T?>
}
