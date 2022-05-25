package io.github.siyual_park.data.criteria

import java.util.regex.Pattern
import kotlin.reflect.KProperty1

class CriteriaStep<T : Any, V : Any?>(
    val key: KProperty1<T, V>
)

fun <T : Any, V : Any?> where(property: KProperty1<T, V>) = CriteriaStep(property)

fun <T : Any, V : Any> CriteriaStep<T, V>.`is`(value: V) = Criteria.Equals(key, value)
fun <T : Any, V : Any> CriteriaStep<T, V>.not(value: V) = Criteria.NotEquals(key, value)

fun <T : Any, V : Comparable<V>> CriteriaStep<T, V>.between(value: ClosedRange<V>) = Criteria.Between(key, value)
fun <T : Any, V : Comparable<V>> CriteriaStep<T, V>.notBetween(value: ClosedRange<V>) = Criteria.NotBetween(key, value)

fun <T : Any, V : Comparable<V>> CriteriaStep<T, V>.lessThan(value: V) = Criteria.LessThan(key, value)
fun <T : Any, V : Comparable<V>> CriteriaStep<T, V>.lessThanOrEquals(value: V) = Criteria.LessThanEquals(key, value)

fun <T : Any, V : Comparable<V>> CriteriaStep<T, V>.greaterThan(value: V) = Criteria.GreaterThan(key, value)
fun <T : Any, V : Comparable<V>> CriteriaStep<T, V>.greaterThanOrEquals(value: V) = Criteria.GreaterThanEquals(key, value)

fun <T : Any, V : Any?> CriteriaStep<T, V>.isNull() = Criteria.IsNull(key)
fun <T : Any, V : Any?> CriteriaStep<T, V>.isNotNull() = Criteria.IsNotNull(key)

fun <T : Any, V : String?> CriteriaStep<T, V>.like(value: String) = Criteria.Like(key, value)
fun <T : Any, V : String?> CriteriaStep<T, V>.notLike(value: String) = Criteria.NotLike(key, value)

fun <T : Any, V : String?> CriteriaStep<T, V>.regexp(value: Pattern) = Criteria.Regexp(key, value)
fun <T : Any, V : String?> CriteriaStep<T, V>.notRegexp(value: Pattern) = Criteria.NotRegexp(key, value)

fun <T : Any, V : Any> CriteriaStep<T, V>.`in`(vararg value: V) = Criteria.In(key, value.toList())
fun <T : Any, V : Any> CriteriaStep<T, V>.notIn(vararg value: V) = Criteria.NotIn(key, value.toList())

fun <T : Any, V : Any> CriteriaStep<T, V>.`in`(value: List<V>) = Criteria.In(key, value)
fun <T : Any, V : Any> CriteriaStep<T, V>.notIn(value: List<V>) = Criteria.NotIn(key, value)

fun <T : Any, V : Boolean?> CriteriaStep<T, V>.isTrue() = Criteria.IsTrue(key)
fun <T : Any, V : Boolean?> CriteriaStep<T, V>.isFalse() = Criteria.IsFalse(key)

fun <T : Any> Criteria<T>.and(value: Criteria<T>) = Criteria.And(mutableListOf(this).apply { add(value) })
fun <T : Any> Criteria<T>.and(value: Collection<Criteria<T>>) = Criteria.And(mutableListOf(this).apply { addAll(value) })
fun <T : Any> Criteria<T>.or(value: Criteria<T>) = Criteria.Or(mutableListOf(this).apply { add(value) })
fun <T : Any> Criteria<T>.or(value: Collection<Criteria<T>>) = Criteria.Or(mutableListOf(this).apply { addAll(value) })
