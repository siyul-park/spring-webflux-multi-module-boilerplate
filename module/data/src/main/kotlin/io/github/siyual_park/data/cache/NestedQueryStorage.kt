package io.github.siyual_park.data.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength

class NestedQueryStorage<T : Any>(
    private val freePool: LoadingPool<QueryStorage<T>>,
    private val usedPool: Pool<QueryStorage<T>> = Pool(ReferenceStrength.HARD),
    override val parent: NestedQueryStorage<T>? = null,
) : QueryStorage<T>, GeneralNestedStorage<NestedQueryStorage<T>> {
    private val mutex = Mutex()
    private val delegator = AsyncLazy { freePool.poll().also { usedPool.add(it) } }

    override suspend fun getIfPresent(where: String): T? {
        return parent?.getIfPresent(where) ?: delegator.get().getIfPresent(where)
    }

    override suspend fun getIfPresent(where: String, loader: suspend () -> T?): T? {
        return parent?.getIfPresent(where) ?: delegator.get().getIfPresent(where, loader)
    }

    override suspend fun getIfPresent(select: SelectQuery): Collection<T>? {
        return parent?.getIfPresent(select) ?: delegator.get().getIfPresent(select)
    }

    override suspend fun getIfPresent(select: SelectQuery, loader: suspend () -> Collection<T>?): Collection<T>? {
        return parent?.getIfPresent(select) ?: delegator.get().getIfPresent(select, loader)
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
        usedPool.forEach { it.clear() }
        delegator.pop()?.let {
            mutex.withLock {
                usedPool.remove(it)
                freePool.add(it)
            }
        }
    }

    override suspend fun entries(): Pair<Set<Pair<String, T>>, Set<Pair<SelectQuery, Collection<T>>>> {
        return delegator.get().entries()
    }

    suspend fun diff(): Pair<Set<Pair<String, T>>, Set<Pair<SelectQuery, Collection<T>>>> {
        return entries().also {
            delegator.pop()?.let {
                mutex.withLock {
                    usedPool.remove(it)
                    it.clear()
                    freePool.add(it)
                }
            }
        }
    }

    override suspend fun fork(): NestedQueryStorage<T> {
        return NestedQueryStorage(freePool, usedPool, parent)
    }

    override suspend fun merge(storage: NestedQueryStorage<T>) {
        val (single, multi) = storage.diff()
        mutex.withLock {
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
}
