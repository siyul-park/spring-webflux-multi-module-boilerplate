package io.github.siyual_park.persistence

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

class ReadOnlyProxy<T : Any, V : Any?>(
    private val lazyMutable: LazyMutable<T>,
    private val property: KProperty1<T, V>
) : ReadOnlyProperty<Any, V> {
    override fun getValue(thisRef: Any, property: KProperty<*>): V {
        return lazyMutable[this.property]
    }
}
