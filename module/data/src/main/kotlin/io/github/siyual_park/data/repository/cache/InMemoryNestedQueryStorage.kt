package io.github.siyual_park.data.repository.cache

import io.github.siyual_park.data.cache.AsyncLazy
import io.github.siyual_park.data.cache.Pool
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryNestedQueryStorage<T : Any>(
    private val pool: Pool<CacheQueryProvider<T>>,
    override val parent: NestedQueryStorage<T>? = null
) : NestedQueryStorage<T> {
    private val cacheProvider = AsyncLazy { pool.poll() }
    private val mutex = Mutex()

    override suspend fun getIfPresent(where: String): T? {
        return parent?.getIfPresent(where) ?: cacheProvider.get().single().getIfPresent(where)
    }

    override suspend fun getIfPresent(where: String, loader: suspend () -> T?): T? {
        return parent?.getIfPresent(where) ?: cacheProvider.get().single().getIfPresent(where, loader)
    }

    override suspend fun getIfPresent(select: SelectQuery): Collection<T>? {
        return parent?.getIfPresent(select) ?: cacheProvider.get().multi().getIfPresent(select)
    }

    override suspend fun getIfPresent(select: SelectQuery, loader: suspend () -> Collection<T>?): Collection<T>? {
        return parent?.getIfPresent(select) ?: cacheProvider.get().multi().getIfPresent(select, loader)
    }

    override suspend fun clear() {
        cacheProvider.get().single().clear()
        cacheProvider.get().multi().clear()

        parent?.clear()

        mutex.withLock {
            pool.add(cacheProvider.get())
            cacheProvider.clear()
        }
    }

    override suspend fun diff(): Pair<Set<Pair<String, T>>, Set<Pair<SelectQuery, Collection<T>>>> {
        return cacheProvider.get().single().entries() to cacheProvider.get().multi().entries()
    }

    override suspend fun fork(): NestedQueryStorage<T> {
        return InMemoryNestedQueryStorage(pool, this)
    }

    override suspend fun merge(storage: NestedQueryStorage<T>) {
        val (single, multi) = storage.diff()
        single.forEach { (key, value) ->
            cacheProvider.get().single().put(key, value)
        }
        multi.forEach { (key, value) ->
            cacheProvider.get().multi().put(key, value)
        }
        storage.clear()
    }
}
