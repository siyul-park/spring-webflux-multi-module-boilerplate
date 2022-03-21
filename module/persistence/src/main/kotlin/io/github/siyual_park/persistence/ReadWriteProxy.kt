package io.github.siyual_park.persistence

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

class ReadWriteProxy<T : Any, V : Any?>(
    private val lazyMutable: LazyMutable<T>,
    private val property: KMutableProperty1<T, V>
) : ReadWriteProperty<Any, V> {
    override fun getValue(thisRef: Any, property: KProperty<*>): V {
        return lazyMutable[this.property]
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: V) {
        lazyMutable[this.property] = value
    }
}
