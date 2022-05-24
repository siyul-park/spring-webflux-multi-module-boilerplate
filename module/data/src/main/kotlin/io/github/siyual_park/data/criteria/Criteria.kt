package io.github.siyual_park.data.criteria

import java.util.regex.Pattern
import kotlin.reflect.KProperty1

sealed class Criteria<T : Any> {
    class Empty<T : Any> : Criteria<T>() {
        override fun toString(): String {
            return "()"
        }
    }

    data class And<T : Any>(val value: List<Criteria<T>>) : Criteria<T>() {
        override fun toString(): String {
            return "(${value.joinToString(" || ") { it.toString() }}})"
        }
    }
    data class Or<T : Any>(val value: List<Criteria<T>>) : Criteria<T>() {
        override fun toString(): String {
            return "(${value.joinToString(" || ") { it.toString() }})"
        }
    }

    data class Equals<T : Any, V : Any>(val key: KProperty1<T, V?>, val value: V) : Criteria<T>() {
        override fun toString(): String {
            return "${key.name} == $value"
        }
    }
    data class NotEquals<T : Any, V : Any>(val key: KProperty1<T, V?>, val value: V) : Criteria<T>() {
        override fun toString(): String {
            return "${key.name} != $value"
        }
    }

    data class Between<T : Any, V : Comparable<V>>(val key: KProperty1<T, V?>, val value: ClosedRange<V>) : Criteria<T>() {
        override fun toString(): String {
            return "(${key.name} >= ${value.start} && ${key.name} <= ${value.endInclusive})"
        }
    }
    data class NotBetween<T : Any, V : Comparable<V>>(val key: KProperty1<T, V?>, val value: ClosedRange<V>) : Criteria<T>() {
        override fun toString(): String {
            return "(${key.name} < ${value.start} || ${key.name} > ${value.endInclusive})"
        }
    }

    data class LessThan<T : Any, V : Comparable<V>>(val key: KProperty1<T, V?>, val value: V) : Criteria<T>() {
        override fun toString(): String {
            return "${key.name} < $value"
        }
    }
    data class LessThanEquals<T : Any, V : Comparable<V>>(val key: KProperty1<T, V?>, val value: V) : Criteria<T>() {
        override fun toString(): String {
            return "${key.name} <= $value"
        }
    }

    data class GreaterThan<T : Any, V : Comparable<V>>(val key: KProperty1<T, V?>, val value: V) : Criteria<T>() {
        override fun toString(): String {
            return "${key.name} > $value"
        }
    }
    data class GreaterThanEquals<T : Any, V : Comparable<V>>(val key: KProperty1<T, V?>, val value: V) : Criteria<T>() {
        override fun toString(): String {
            return "${key.name} >= $value"
        }
    }

    data class IsNull<T : Any, V : Any?>(val key: KProperty1<T, V>) : Criteria<T>() {
        override fun toString(): String {
            return "${key.name} == null"
        }
    }
    data class IsNotNull<T : Any, V : Any?>(val key: KProperty1<T, V>) : Criteria<T>() {
        override fun toString(): String {
            return "${key.name} != null"
        }
    }

    data class Like<T : Any>(val key: KProperty1<T, String>, val value: String) : Criteria<T>() {
        override fun toString(): String {
            return "${key.name} like $value"
        }
    }
    data class NotLike<T : Any>(val key: KProperty1<T, String>, val value: String) : Criteria<T>() {
        override fun toString(): String {
            return "${key.name} !like $value"
        }
    }

    data class Regexp<T : Any>(val key: KProperty1<T, String>, val value: Pattern) : Criteria<T>() {
        override fun toString(): String {
            return "${key.name} regexp $value"
        }
    }
    data class NotRegexp<T : Any>(val key: KProperty1<T, String>, val value: Pattern) : Criteria<T>() {
        override fun toString(): String {
            return "${key.name} !regexp $value"
        }
    }

    data class In<T : Any, V : Any>(val key: KProperty1<T, V>, val value: List<V>) : Criteria<T>() {
        override fun toString(): String {
            return "${key.name} in (${value.map { it.toString() }.joinToString { ", " }})"
        }
    }
    data class NotIn<T : Any, V : Any>(val key: KProperty1<T, V>, val value: List<V>) : Criteria<T>() {
        override fun toString(): String {
            return "${key.name} not in (${value.map { it.toString() }.joinToString { ", " }})"
        }
    }

    data class IsTrue<T : Any>(val key: KProperty1<T, Boolean?>) : Criteria<T>() {
        override fun toString(): String {
            return "${key.name} == true"
        }
    }
    data class IsFalse<T : Any>(val key: KProperty1<T, Boolean?>) : Criteria<T>() {
        override fun toString(): String {
            return "${key.name} == false"
        }
    }
}
