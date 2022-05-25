package io.github.siyual_park.data.criteria

import java.util.regex.Pattern

sealed class Criteria {
    object Empty : Criteria() {
        override fun toString(): String {
            return "()"
        }
    }

    data class And(val value: List<Criteria>) : Criteria() {
        override fun toString(): String {
            return "(${value.joinToString(" AND ") { it.toString() }})"
        }
    }
    data class Or(val value: List<Criteria>) : Criteria() {
        override fun toString(): String {
            return "(${value.joinToString(" OR ") { it.toString() }})"
        }
    }

    data class Equals(val key: String, val value: Any?) : Criteria() {
        override fun toString(): String {
            return "$key = $value"
        }
    }
    data class NotEquals(val key: String, val value: Any?) : Criteria() {
        override fun toString(): String {
            return "$key != $value"
        }
    }

    data class Between(val key: String, val value: ClosedRange<*>) : Criteria() {
        override fun toString(): String {
            return "($key >= ${value.start} && $key <= ${value.endInclusive})"
        }
    }
    data class NotBetween(val key: String, val value: ClosedRange<*>) : Criteria() {
        override fun toString(): String {
            return "($key < ${value.start} || $key > ${value.endInclusive})"
        }
    }

    data class LessThan(val key: String, val value: Any) : Criteria() {
        override fun toString(): String {
            return "$key < $value"
        }
    }
    data class LessThanEquals(val key: String, val value: Any) : Criteria() {
        override fun toString(): String {
            return "$key <= $value"
        }
    }

    data class GreaterThan(val key: String, val value: Any) : Criteria() {
        override fun toString(): String {
            return "$key > $value"
        }
    }
    data class GreaterThanEquals(val key: String, val value: Any) : Criteria() {
        override fun toString(): String {
            return "$key >= $value"
        }
    }

    data class IsNull(val key: String) : Criteria() {
        override fun toString(): String {
            return "$key = null"
        }
    }
    data class IsNotNull(val key: String) : Criteria() {
        override fun toString(): String {
            return "$key != null"
        }
    }

    data class Like(val key: String, val value: String) : Criteria() {
        override fun toString(): String {
            return "$key LIKE $value"
        }
    }
    data class NotLike(val key: String, val value: String) : Criteria() {
        override fun toString(): String {
            return "$key NOT LIKE $value"
        }
    }

    data class Regexp(val key: String, val value: Pattern) : Criteria() {
        override fun toString(): String {
            return "$key REGEXP $value"
        }
    }
    data class NotRegexp(val key: String, val value: Pattern) : Criteria() {
        override fun toString(): String {
            return "$key NOT REGEXP $value"
        }
    }

    data class In(val key: String, val value: List<Any?>) : Criteria() {
        override fun toString(): String {
            return "$key IN (${value.map { it.toString() }.joinToString { ", " }})"
        }
    }
    data class NotIn(val key: String, val value: List<Any?>) : Criteria() {
        override fun toString(): String {
            return "$key NOT IN (${value.map { it.toString() }.joinToString { ", " }})"
        }
    }

    data class IsTrue(val key: String) : Criteria() {
        override fun toString(): String {
            return "$key IS TRUE"
        }
    }
    data class IsFalse(val key: String) : Criteria() {
        override fun toString(): String {
            return "$key IS FALSE"
        }
    }
}
