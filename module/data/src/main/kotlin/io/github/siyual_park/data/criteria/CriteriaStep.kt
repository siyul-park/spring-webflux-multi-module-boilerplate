package io.github.siyual_park.data.criteria

import java.util.regex.Pattern
import kotlin.reflect.KProperty1

data class CriteriaStep(
    val key: String
) {
    fun `is`(value: Any?) = Criteria.Equals(key, value)
    fun not(value: Any?) = Criteria.NotEquals(key, value)

    fun between(value: ClosedRange<*>) = Criteria.Between(key, value)
    fun notBetween(value: ClosedRange<*>) = Criteria.NotBetween(key, value)

    fun lessThan(value: Any) = Criteria.LessThan(key, value)
    fun lessThanOrEquals(value: Any) = Criteria.LessThanEquals(key, value)

    fun greaterThan(value: Any) = Criteria.GreaterThan(key, value)
    fun greaterThanOrEquals(value: Any) = Criteria.GreaterThanEquals(key, value)

    fun isNull() = Criteria.IsNull(key)
    fun isNotNull() = Criteria.IsNotNull(key)

    fun like(value: String) = Criteria.Like(key, value)
    fun notLike(value: String) = Criteria.NotLike(key, value)

    fun regexp(value: Pattern) = Criteria.Regexp(key, value)
    fun notRegexp(value: Pattern) = Criteria.NotRegexp(key, value)

    fun `in`(vararg value: Any?) = Criteria.In(key, value.toList())
    fun notIn(vararg value: Any?) = Criteria.NotIn(key, value.toList())

    fun `in`(value: List<Any?>) = Criteria.In(key, value)
    fun notIn(value: List<Any?>) = Criteria.NotIn(key, value)

    fun isTrue() = Criteria.IsTrue(key)
    fun isFalse() = Criteria.IsFalse(key)
}

fun <T, V> where(property: KProperty1<T, V>) = CriteriaStep(property.name)
fun where(key: String) = CriteriaStep(key)

fun Criteria.and(value: Criteria) = Criteria.And(mutableListOf(this).apply { add(value) })
fun Criteria.and(value: Collection<Criteria>) = Criteria.And(mutableListOf(this).apply { addAll(value) })
fun Criteria.or(value: Criteria) = Criteria.Or(mutableListOf(this).apply { add(value) })
fun Criteria.or(value: Collection<Criteria>) = Criteria.Or(mutableListOf(this).apply { addAll(value) })
