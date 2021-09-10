package io.github.siyual_park.data.repository.in_memory.callback

import org.springframework.dao.DuplicateKeyException
import kotlin.reflect.KProperty

class Index<T : Any, INDEX : Any, ID : Any?>(
    private val indexProperty: KProperty<INDEX>,
    private val idProperty: KProperty<ID>
) : EntityCallbacks<T> {
    private val index = mutableMapOf<INDEX, ID>()

    override fun onCreate(entity: T) {
        val key = indexProperty.call(entity)
        if (index.containsKey(key)) {
            throw DuplicateKeyException("${indexProperty.name}: $key is already existed")
        }
        index[key] = idProperty.call(entity)
    }

    override fun onDelete(entity: T) {
        index.remove(indexProperty.call(entity))
    }

    override fun onUpdate(origin: T, entity: T) {
        val originKey = indexProperty.call(origin)
        val key = indexProperty.call(entity)

        if (originKey != key) {
            index.remove(originKey)
            index[key] = idProperty.call(entity)
        }
    }

    operator fun get(key: INDEX): ID? {
        return index[key]
    }
}
