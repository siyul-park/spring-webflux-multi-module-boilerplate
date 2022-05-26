package io.github.siyual_park.data.cache

class TransactionalQueryStorage<T : Any>(
    root: NestedQueryStorage<T>,
) : QueryStorage<T> {
    private val provider = TransactionalStorageProvider(root)

    override suspend fun getIfPresent(select: SelectQuery): Collection<T>? {
        return provider.get().getIfPresent(select)
    }

    override suspend fun getIfPresent(select: SelectQuery, loader: suspend () -> Collection<T>?): Collection<T>? {
        return provider.get().getIfPresent(select, loader)
    }

    override suspend fun remove(select: SelectQuery) {
        return provider.get().remove(select)
    }

    override suspend fun put(select: SelectQuery, value: Collection<T>) {
        return provider.get().put(select, value)
    }

    override suspend fun entries(): Set<Pair<SelectQuery, Collection<T>>> {
        return provider.get().entries()
    }

    override suspend fun clear(entity: T) {
        return provider.get().clear(entity)
    }

    override suspend fun clear() {
        return provider.get().clear()
    }
}
