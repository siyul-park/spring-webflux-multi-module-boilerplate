package io.github.siyual_park.data.criteria

import kotlin.reflect.KProperty1

sealed class Criteria<T : Any> {
    data class And<T : Any>(val value: Collection<Criteria<T>>) : Criteria<T>()
    data class Or<T : Any>(val value: Collection<Criteria<T>>) : Criteria<T>()

    data class Equals<T : Any, V : Any?>(val key: KProperty1<T, V>, val value: V) : Criteria<T>()
    data class NotEquals<T : Any, V : Any?>(val key: KProperty1<T, V>, val value: V) : Criteria<T>()

    data class Between<T : Any, V : Comparable<V>>(val key: KProperty1<T, V?>, val value: ClosedRange<V>) : Criteria<T>()
    data class NotBetween<T : Any, V : Comparable<V>>(val key: KProperty1<T, V?>, val value: ClosedRange<V>) : Criteria<T>()

    data class LessThan<T : Any, V : Comparable<V>>(val key: KProperty1<T, V?>, val value: V) : Criteria<T>()
    data class LessThanEquals<T : Any, V : Comparable<V>>(val key: KProperty1<T, V?>, val value: V) : Criteria<T>()

    data class GreaterThan<T : Any, V : Comparable<V>>(val key: KProperty1<T, V?>, val value: V) : Criteria<T>()
    data class GreaterThanEquals<T : Any, V : Comparable<V>>(val key: KProperty1<T, V?>, val value: V) : Criteria<T>()

    data class IsNull<T : Any, V : Any?>(val key: KProperty1<T, V>) : Criteria<T>()
    data class NotNull<T : Any, V : Any?>(val key: KProperty1<T, V>) : Criteria<T>()

    data class Like<T : Any>(val key: KProperty1<T, String?>) : Criteria<T>()
    data class NotLike<T : Any>(val key: KProperty1<T, String?>) : Criteria<T>()

    data class In<T : Any, V : Any?>(val key: KProperty1<T, V>, val value: Collection<V>) : Criteria<T>()
    data class NotIn<T : Any, V : Any?>(val key: KProperty1<T, V>, val value: Collection<V>) : Criteria<T>()

    data class IsTrue<T : Any>(val key: KProperty1<T, Boolean?>) : Criteria<T>()
    data class IsFalse<T : Any>(val key: KProperty1<T, Boolean?>) : Criteria<T>()
}
