package io.github.siyual_park.data.cache

class PoolingNestedQueryStorage<T : Any>(
    private val pool: Pool<QueryStorage<T>>,
    override val parent: PoolingNestedQueryStorage<T>? = null,
) : NestedQueryStorage<T> {
    private val delegator = SuspendLazy { pool.pop().also { it.clear() } }

    override suspend fun getIfPresent(select: SelectQuery, loader: suspend () -> Collection<T>?): Collection<T>? {
        return getIfPresent(select) ?: loader()?.also { put(select, it) }
    }

    override suspend fun getIfPresent(select: SelectQuery): Collection<T>? {
        return delegator.get().getIfPresent(select) ?: parent?.getIfPresent(select)
    }

    override suspend fun remove(select: SelectQuery) {
        delegator.get().remove(select)
    }

    override suspend fun put(select: SelectQuery, value: Collection<T>) {
        delegator.get().put(select, value)
    }

    override suspend fun clear(entity: T) {
        pool.used().entries().forEach { it.clear(entity) }
    }

    override suspend fun clear() {
        pool.used().entries().forEach { it.clear() }
        delegator.pop()?.let {
            pool.push(it)
        }
    }

    override suspend fun checkout(): Set<Pair<SelectQuery, Collection<T>>> {
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
        val multi = storage.checkout()
        delegator.get().also {
            multi.forEach { (key, value) ->
                it.put(key, value)
            }
        }
    }

    override suspend fun entries(): Set<Pair<SelectQuery, Collection<T>>> {
        return delegator.get().entries()
    }
}
