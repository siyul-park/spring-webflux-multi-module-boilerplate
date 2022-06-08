package io.github.siyual_park.data.cache

import io.github.siyual_park.data.WeekProperty

@Suppress("UNCHECKED_CAST")
class MultiLevelNestedStorage<ID : Any, T : Any>(
    private val primary: Storage<ID, T>,
    private val secondary: Pool<Storage<ID, T>>,
    private val id: WeekProperty<T, ID?>,
) : NestedStorage<ID, T>, Storage<ID, T> by primary {
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
}
