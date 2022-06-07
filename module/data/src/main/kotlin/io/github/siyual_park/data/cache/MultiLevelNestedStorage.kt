package io.github.siyual_park.data.cache

import io.github.siyual_park.data.WeekProperty

@Suppress("UNCHECKED_CAST")
class MultiLevelNestedStorage<ID : Any, T : Any>(
    private val primary: Storage<ID, T>,
    private val secondary: Pool<Storage<ID, T>>,
    private val id: WeekProperty<T, ID?>,
) : NestedStorage<ID, T> {
    override val parent: NestedStorage<ID, T>? = null

    private val delegator = SuspendLazy {
        MultiLevelStorage(primary).apply {
            register(secondary.pop())
        }
    }

    override suspend fun checkout(): Map<ID, T?> {
        return delegator.get().entries().toMap()
    }

    override suspend fun fork(): NestedStorage<ID, T> {
        return PoolingNestedStorage(secondary, id, this).also {
            getIndexes().forEach { (name, extractor) ->
                it.createIndex(name, extractor as WeekProperty<T, Any>)
            }
        }
    }

    override suspend fun merge(storage: NestedStorage<ID, T>) {
        val diff = storage.checkout()

        diff.forEach { (key, value) ->
            if (value != null) {
                add(value)
            } else {
                remove(key)
            }
        }
    }

    override suspend fun <KEY : Any> createIndex(name: String, property: WeekProperty<T, KEY>) {
        delegator.get().createIndex(name, property)
    }

    override suspend fun removeIndex(name: String) {
        delegator.get().removeIndex(name)
    }

    override suspend fun getIndexes(): Map<String, WeekProperty<T, *>> {
        return delegator.get().getIndexes()
    }

    override suspend fun containsIndex(name: String): Boolean {
        return delegator.get().containsIndex(name)
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY, loader: suspend () -> T?): T? {
        return delegator.get().getIfPresent(index, key, loader)
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY): T? {
        return delegator.get().getIfPresent(index, key)
    }

    override suspend fun getIfPresent(id: ID, loader: suspend () -> T?): T? {
        return delegator.get().getIfPresent(id, loader)
    }

    override suspend fun getIfPresent(id: ID): T? {
        return delegator.get().getIfPresent(id)
    }

    override suspend fun remove(id: ID) {
        delegator.get().remove(id)
    }

    override suspend fun add(entity: T) {
        delegator.get().add(entity)
    }

    override suspend fun clear() {
        delegator.get().clear()
    }

    override suspend fun entries(): Set<Pair<ID, T>> {
        return delegator.get().entries()
    }
}
