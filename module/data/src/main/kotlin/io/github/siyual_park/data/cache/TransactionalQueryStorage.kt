package io.github.siyual_park.data.cache

class TransactionalQueryStorage<T : Any>(
    root: NestedQueryStorage<T>,
) : QueryStorage<T> {
    private val provider = TransactionalStorageProvider(root)

    override suspend fun getIfPresent(where: String): T? {
        return provider.get().getIfPresent(where)
    }

    override suspend fun getIfPresent(where: String, loader: suspend () -> T?): T? {
        return provider.get().getIfPresent(where, loader)
    }

    override suspend fun getIfPresent(select: SelectQuery): Collection<T>? {
        return provider.get().getIfPresent(select)
    }

    override suspend fun getIfPresent(select: SelectQuery, loader: suspend () -> Collection<T>?): Collection<T>? {
        return provider.get().getIfPresent(select, loader)
    }

    override suspend fun remove(where: String) {
        return provider.get().remove(where)
    }

    override suspend fun remove(select: SelectQuery) {
        return provider.get().remove(select)
    }

    override suspend fun put(where: String, value: T) {
        return provider.get().put(where, value)
    }

    override suspend fun put(select: SelectQuery, value: Collection<T>) {
        return provider.get().put(select, value)
    }

    override suspend fun entries(): Pair<Set<Pair<String, T>>, Set<Pair<SelectQuery, Collection<T>>>> {
        return provider.get().entries()
    }

    override suspend fun clear() {
        return provider.get().clear()
    }
}
