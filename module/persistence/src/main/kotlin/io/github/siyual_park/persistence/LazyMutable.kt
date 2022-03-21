package io.github.siyual_park.persistence

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

interface LazyMutable<T : Any> {
    operator fun <V : Any?> get(property: KProperty1<T, V>): V
    operator fun <V : Any?> set(property: KMutableProperty1<T, V>, value: V)

    fun raw(): T

    fun isUpdated(): Boolean

    fun clear()

    fun checkout(): Map<KMutableProperty1<T, Any?>, Any?>

    companion object
}
