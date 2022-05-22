package io.github.siyual_park.data.cache

class PoolingNestedQueryStorage<T : Any>(
    private val pool: Pool<QueryStorage<T>>,
    override val parent: PoolingNestedQueryStorage<T>? = null,
) : NestedQueryStorage<T> {
    private val delegator = AsyncLazy { pool.pop().also { it.clear() } }

    override suspend fun getIfPresent(where: String, loader: suspend () -> T?): T? {
        return getIfPresent(where) ?: loader()?.also { put(where, it) }
    }

    override suspend fun getIfPresent(where: String): T? {
        return delegator.get().getIfPresent(where) ?: parent?.getIfPresent(where)
    }

    override suspend fun getIfPresent(select: SelectQuery, loader: suspend () -> Collection<T>?): Collection<T>? {
        return getIfPresent(select) ?: loader()?.also { put(select, it) }
    }

    override suspend fun getIfPresent(select: SelectQuery): Collection<T>? {
        return delegator.get().getIfPresent(select) ?: parent?.getIfPresent(select)
    }

    override suspend fun remove(where: String) {
        delegator.get().remove(where)
    }

    override suspend fun remove(select: SelectQuery) {
        delegator.get().remove(select)
    }

    override suspend fun put(where: String, value: T) {
        delegator.get().put(where, value)
    }

    override suspend fun put(select: SelectQuery, value: Collection<T>) {
        delegator.get().put(select, value)
    }

    override suspend fun clear() {
        pool.used().forEach { it.clear() }
        delegator.pop()?.let {
            pool.push(it)
        }
    }

    override suspend fun entries(): Pair<Set<Pair<String, T>>, Set<Pair<SelectQuery, Collection<T>>>> {
        return delegator.get().entries()
    }

    override suspend fun diff(): Pair<Set<Pair<String, T>>, Set<Pair<SelectQuery, Collection<T>>>> {
        return entries().also {
            delegator.pop()?.let {
                it.clear()
                pool.push(it)
            }
        }
    }

    override suspend fun fork(): PoolingNestedQueryStorage<T> {
        return PoolingNestedQueryStorage(pool, this)
    }

    override suspend fun merge(storage: NestedQueryStorage<T>) {
        val (single, multi) = storage.diff()
        delegator.get().also {
            single.forEach { (key, value) ->
                it.put(key, value)
            }
            multi.forEach { (key, value) ->
                it.put(key, value)
            }
        }
    }
}
