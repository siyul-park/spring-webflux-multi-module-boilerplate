package io.github.siyual_park.data.cache

interface NestedQueryStorage<T : Any> : QueryStorage<T>, GeneralNestedStorage<NestedQueryStorage<T>> {
    suspend fun checkout(): Set<Pair<SelectQuery, Collection<T>>>
}
