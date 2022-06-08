package io.github.siyual_park.data.cache

import io.github.siyual_park.data.WeekProperty

@Suppress("UNCHECKED_CAST")
class MultiLevelNestedStorage<ID : Any, T : Any>(
    private val primary: Storage<ID, T>,
    private val secondary: Pool<Storage<ID, T>>,
    private val id: WeekProperty<T, ID?>,
) : NestedStorage<ID, T> {
    override val parent: NestedStorage<ID, T>? = null

    override suspend fun checkout(): Map<ID, T?> {
        return primary.entries().toMap()
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
        primary.createIndex(name, property)
    }

    override suspend fun removeIndex(name: String) {
        primary.removeIndex(name)
    }

    override suspend fun getIndexes(): Map<String, WeekProperty<T, *>> {
        return primary.getIndexes()
    }

    override suspend fun containsIndex(name: String): Boolean {
        return primary.containsIndex(name)
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY, loader: suspend () -> T?): T? {
        return primary.getIfPresent(index, key, loader)
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY): T? {
        return primary.getIfPresent(index, key)
    }

    override suspend fun getIfPresent(id: ID, loader: suspend () -> T?): T? {
        return primary.getIfPresent(id, loader)
    }

    override suspend fun getIfPresent(id: ID): T? {
        return primary.getIfPresent(id)
    }

    override suspend fun remove(id: ID) {
        primary.remove(id)
    }

    override suspend fun add(entity: T) {
        primary.add(entity)
    }

    override suspend fun clear() {
        primary.clear()
    }

    override suspend fun entries(): Set<Pair<ID, T>> {
        return primary.entries()
    }
}
