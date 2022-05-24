package io.github.siyual_park.data.criteria

import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

sealed class Criteria {
    data class And(val value: Collection<Criteria>) : Criteria() {
        override fun toString(): String {
            return "(${value.map { it.toString() }.joinToString { " && " }})"
        }
    }
    data class Or(val value: Collection<Criteria>) : Criteria() {
        override fun toString(): String {
            return "(${value.map { it.toString() }.joinToString { " || " }})"
        }
    }

    data class Equals<V : Any?>(val key: KProperty<V>, val value: V) : Criteria() {
        override fun toString(): String {
            return "${key.name} == $value"
        }
    }
    data class NotEquals<V : Any?>(val key: KProperty<V>, val value: V) : Criteria() {
        override fun toString(): String {
            return "${key.name} != $value"
        }
    }

    data class Between<V : Comparable<V>>(val key: KProperty<V?>, val value: ClosedRange<V>) : Criteria() {
        override fun toString(): String {
            return "(${key.name} >= ${value.start} && ${key.name} <= ${value.endInclusive})"
        }
    }
    data class NotBetween<V : Comparable<V>>(val key: KProperty<V?>, val value: ClosedRange<V>) : Criteria() {
        override fun toString(): String {
            return "(${key.name} < ${value.start} || ${key.name} > ${value.endInclusive})"
        }
    }

    data class LessThan<V : Comparable<V>>(val key: KProperty<V?>, val value: V) : Criteria() {
        override fun toString(): String {
            return "${key.name} < $value"
        }
    }
    data class LessThanEquals<V : Comparable<V>>(val key: KProperty<V?>, val value: V) : Criteria() {
        override fun toString(): String {
            return "${key.name} <= $value"
        }
    }

    data class GreaterThan<V : Comparable<V>>(val key: KProperty<V?>, val value: V) : Criteria() {
        override fun toString(): String {
            return "${key.name} > $value"
        }
    }
    data class GreaterThanEquals<V : Comparable<V>>(val key: KProperty<V?>, val value: V) : Criteria() {
        override fun toString(): String {
            return "${key.name} >= $value"
        }
    }

    data class IsNull<V : Any?>(val key: KProperty<V>) : Criteria() {
        override fun toString(): String {
            return "${key.name} == null"
        }
    }
    data class NotNull<V : Any?>(val key: KProperty<V>) : Criteria() {
        override fun toString(): String {
            return "${key.name} != null"
        }
    }

    data class Like<T : Any>(val key: KProperty1<T, String?>, val value: String) : Criteria() {
        override fun toString(): String {
            return "${key.name} like $value"
        }
    }
    data class NotLike<T : Any>(val key: KProperty1<T, String?>, val value: String) : Criteria() {
        override fun toString(): String {
            return "${key.name} !like $value"
        }
    }

    data class In<V : Any?>(val key: KProperty<V>, val value: Collection<V>) : Criteria() {
        override fun toString(): String {
            return "${key.name} in (${value.map { it.toString() }.joinToString { ", " }})"
        }
    }
    data class NotIn<V : Any?>(val key: KProperty<V>, val value: Collection<V>) : Criteria() {
        override fun toString(): String {
            return "${key.name} not in (${value.map { it.toString() }.joinToString { ", " }})"
        }
    }

    data class IsTrue<T : Any>(val key: KProperty1<T, Boolean?>) : Criteria() {
        override fun toString(): String {
            return "${key.name} == true"
        }
    }
    data class IsFalse<T : Any>(val key: KProperty1<T, Boolean?>) : Criteria() {
        override fun toString(): String {
            return "${key.name} == false"
        }
    }
}
