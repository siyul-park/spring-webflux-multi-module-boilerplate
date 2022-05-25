package io.github.siyual_park.data.criteria

import io.github.siyual_park.data.regexp.SqlLikeTranspiler
import java.util.regex.Pattern

@Suppress("UNCHECKED_CAST")
class FilterCriteriaParser<T : Any> : CriteriaParser<T, ((T) -> Boolean)?> {
    override fun parse(criteria: Criteria<T>): ((T) -> Boolean)? {
        return when (criteria) {
            is Criteria.Empty -> null
            is Criteria.And -> parse(criteria)
            is Criteria.Or -> parse(criteria)
            is Criteria.Equals<T, *> -> parse(criteria)
            is Criteria.NotEquals<T, *> -> parse(criteria)
            is Criteria.Between<T, *> -> parse(criteria)
            is Criteria.NotBetween<T, *> -> parse(criteria)
            is Criteria.LessThan<T, *> -> parse(criteria)
            is Criteria.LessThanEquals<T, *> -> parse(criteria)
            is Criteria.GreaterThan<T, *> -> parse(criteria)
            is Criteria.GreaterThanEquals<T, *> -> parse(criteria)
            is Criteria.IsNull<T, *> -> parse(criteria)
            is Criteria.IsNotNull<T, *> -> parse(criteria)
            is Criteria.Like<T, *> -> parse(criteria)
            is Criteria.NotLike<T, *> -> parse(criteria)
            is Criteria.Regexp<T, *> -> parse(criteria)
            is Criteria.NotRegexp<T, *> -> parse(criteria)
            is Criteria.In<T, *> -> parse(criteria)
            is Criteria.NotIn<T, *> -> parse(criteria)
            is Criteria.IsTrue<T, *> -> parse(criteria)
            is Criteria.IsFalse<T, *> -> parse(criteria)
        }
    }

    private fun parse(criteria: Criteria.And<T>): ((T) -> Boolean)? {
        if (criteria.value.isEmpty()) {
            return null
        }
        if (criteria.value.size == 1) {
            return parse(criteria.value[0])
        }
        return criteria.value.mapNotNull { parse(it) }
            .reduce { acc, cur -> { acc(it) && cur(it) } }
    }
    private fun parse(criteria: Criteria.Or<T>): ((T) -> Boolean)? {
        if (criteria.value.isEmpty()) {
            return null
        }
        if (criteria.value.size == 1) {
            return parse(criteria.value[0])
        }
        return criteria.value.mapNotNull { parse(it) }
            .reduce { acc, cur -> { acc(it) || cur(it) } }
    }

    private fun parse(criteria: Criteria.Equals<T, *>): (T) -> Boolean {
        return { criteria.key.get(it)?.equals(criteria.value) == true }
    }
    private fun parse(criteria: Criteria.NotEquals<T, *>): (T) -> Boolean {
        return { criteria.key.get(it)?.equals(criteria.value) == false }
    }

    private fun parse(criteria: Criteria.Between<T, *>): (T) -> Boolean {
        return {
            criteria.key.get(it)?.let { value ->
                value as Comparable<Any?>
                value >= criteria.value.start && value <= criteria.value.endInclusive
            } ?: false
        }
    }
    private fun parse(criteria: Criteria.NotBetween<T, *>): (T) -> Boolean {
        return {
            criteria.key.get(it)?.let { value ->
                value as Comparable<Any?>
                value < criteria.value.start || value > criteria.value.endInclusive
            } ?: false
        }
    }

    private fun parse(criteria: Criteria.LessThan<T, *>): (T) -> Boolean {
        return {
            criteria.key.get(it)?.let { value ->
                value as Comparable<Any?>
                value < criteria.value
            } ?: false
        }
    }
    private fun parse(criteria: Criteria.LessThanEquals<T, *>): (T) -> Boolean {
        return {
            criteria.key.get(it)?.let { value ->
                value as Comparable<Any?>
                value <= criteria.value
            } ?: false
        }
    }

    private fun parse(criteria: Criteria.GreaterThan<T, *>): (T) -> Boolean {
        return {
            criteria.key.get(it)?.let { value ->
                value as Comparable<Any?>
                value > criteria.value
            } ?: false
        }
    }
    private fun parse(criteria: Criteria.GreaterThanEquals<T, *>): (T) -> Boolean {
        return {
            criteria.key.get(it)?.let { value ->
                value as Comparable<Any?>
                value >= criteria.value
            } ?: false
        }
    }

    private fun parse(criteria: Criteria.IsNull<T, *>): (T) -> Boolean {
        return { criteria.key.get(it) == null }
    }
    private fun parse(criteria: Criteria.IsNotNull<T, *>): (T) -> Boolean {
        return { criteria.key.get(it) != null }
    }

    private fun parse(criteria: Criteria.Like<T, *>): (T) -> Boolean {
        val pattern = Pattern.compile(SqlLikeTranspiler.toRegEx(criteria.value))
        return { criteria.key.get(it)?.let { pattern.matcher(it).find() } ?: false }
    }
    private fun parse(criteria: Criteria.NotLike<T, *>): (T) -> Boolean {
        return { criteria.key.get(it) != null }
    }

    private fun parse(criteria: Criteria.Regexp<T, *>): (T) -> Boolean {
        return { criteria.key.get(it) != null }
    }
    private fun parse(criteria: Criteria.NotRegexp<T, *>): (T) -> Boolean {
        return { criteria.key.get(it) != null }
    }

    private fun parse(criteria: Criteria.In<T, *>): (T) -> Boolean {
        return { criteria.key.get(it)?.let { criteria.value.contains(it) } ?: false }
    }
    private fun parse(criteria: Criteria.NotIn<T, *>): (T) -> Boolean {
        return { criteria.key.get(it)?.let { !criteria.value.contains(it) } ?: false }
    }

    private fun parse(criteria: Criteria.IsTrue<T, *>): (T) -> Boolean {
        return { criteria.key.get(it) == true }
    }
    private fun parse(criteria: Criteria.IsFalse<T, *>): (T) -> Boolean {
        return { criteria.key.get(it) == false }
    }
}
