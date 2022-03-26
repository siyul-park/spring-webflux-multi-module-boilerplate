package io.github.siyual_park.persistence

import org.springframework.dao.EmptyResultDataAccessException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

class NotNullReadWriteProxy<T : Any, V : Any>(
    private val lazyMutable: LazyMutable<T>,
    private val property: KMutableProperty1<T, V?>
) : ReadWriteProperty<Any, V> {
    override fun getValue(thisRef: Any, property: KProperty<*>): V {
        return lazyMutable[this.property] ?: throw EmptyResultDataAccessException(1)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: V) {
        lazyMutable[this.property] = value
    }
}
