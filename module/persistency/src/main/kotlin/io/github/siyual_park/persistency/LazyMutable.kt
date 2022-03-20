package io.github.siyual_park.persistency

import io.github.siyual_park.data.patch.Patch
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

interface LazyMutable<T : Any> {
    operator fun <V : Any?> get(property: KProperty1<T, V>): V
    operator fun <V : Any?> set(property: KMutableProperty1<T, V>, value: V)

    fun getValue(): T
    fun isUpdated(): Boolean

    fun clear()
    fun toPatch(): Patch<T>

    companion object
}
