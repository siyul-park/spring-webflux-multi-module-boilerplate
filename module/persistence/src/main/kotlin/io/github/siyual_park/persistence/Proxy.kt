package io.github.siyual_park.persistence

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

fun <T : Any, V : Any?> proxy(
    lazyMutable: LazyMutable<T>,
    property: KProperty1<T, V>
) = ReadOnlyProxy(lazyMutable, property)

fun <T : Any, V : Any> proxyNotNull(
    lazyMutable: LazyMutable<T>,
    property: KProperty1<T, V?>
) = NotNullReadOnlyProperty(lazyMutable, property)

fun <T : Any, V : Any?> proxy(
    lazyMutable: LazyMutable<T>,
    property: KMutableProperty1<T, V>
) = ReadWriteProxy(lazyMutable, property)

fun <T : Any, V : Any> proxyNotNull(
    lazyMutable: LazyMutable<T>,
    property: KMutableProperty1<T, V?>
) = NotNullReadWriteProxy(lazyMutable, property)
