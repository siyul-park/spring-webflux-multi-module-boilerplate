package io.github.siyual_park.data.criteria

import java.util.regex.Pattern
import kotlin.reflect.KProperty1

class CriteriaStep(
    val key: String
)

fun <T, V> where(property: KProperty1<T, V>) = CriteriaStep(property.name)
fun where(key: String) = CriteriaStep(key)

fun CriteriaStep.`is`(value: Any?) = Criteria.Equals(key, value)
fun CriteriaStep.not(value: Any?) = Criteria.NotEquals(key, value)

fun CriteriaStep.between(value: ClosedRange<*>) = Criteria.Between(key, value)
fun CriteriaStep.notBetween(value: ClosedRange<*>) = Criteria.NotBetween(key, value)

fun CriteriaStep.lessThan(value: Any) = Criteria.LessThan(key, value)
fun CriteriaStep.lessThanOrEquals(value: Any) = Criteria.LessThanEquals(key, value)

fun CriteriaStep.greaterThan(value: Any) = Criteria.GreaterThan(key, value)
fun CriteriaStep.greaterThanOrEquals(value: Any) = Criteria.GreaterThanEquals(key, value)

fun CriteriaStep.isNull() = Criteria.IsNull(key)
fun CriteriaStep.isNotNull() = Criteria.IsNotNull(key)

fun CriteriaStep.like(value: String) = Criteria.Like(key, value)
fun CriteriaStep.notLike(value: String) = Criteria.NotLike(key, value)

fun CriteriaStep.regexp(value: Pattern) = Criteria.Regexp(key, value)
fun CriteriaStep.notRegexp(value: Pattern) = Criteria.NotRegexp(key, value)

fun CriteriaStep.`in`(vararg value: Any?) = Criteria.In(key, value.toList())
fun CriteriaStep.notIn(vararg value: Any?) = Criteria.NotIn(key, value.toList())

fun CriteriaStep.`in`(value: List<Any?>) = Criteria.In(key, value)
fun CriteriaStep.notIn(value: List<Any?>) = Criteria.NotIn(key, value)

fun CriteriaStep.isTrue() = Criteria.IsTrue(key)
fun CriteriaStep.isFalse() = Criteria.IsFalse(key)

fun Criteria.and(value: Criteria) = Criteria.And(mutableListOf(this).apply { add(value) })
fun Criteria.and(value: Collection<Criteria>) = Criteria.And(mutableListOf(this).apply { addAll(value) })
fun Criteria.or(value: Criteria) = Criteria.Or(mutableListOf(this).apply { add(value) })
fun Criteria.or(value: Collection<Criteria>) = Criteria.Or(mutableListOf(this).apply { addAll(value) })
