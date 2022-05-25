package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.criteria.Criteria
import io.github.siyual_park.data.criteria.CriteriaParser
import io.github.siyual_park.data.expansion.columnName
import org.springframework.data.relational.core.query.Criteria as R2DBCCriteria

class R2DBCCriteriaParser<T : Any> : CriteriaParser<T, R2DBCCriteria> {
    override fun parse(criteria: Criteria<T>): R2DBCCriteria {
        return when (criteria) {
            is Criteria.Empty -> R2DBCCriteria.empty()
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

    private fun parse(criteria: Criteria.And<T>): R2DBCCriteria {
        if (criteria.value.isEmpty()) {
            return R2DBCCriteria.empty()
        }
        if (criteria.value.size == 1) {
            return parse(criteria.value[0])
        }
        return criteria.value
            .filter { it !is Criteria.Empty }
            .map { parse(it) }
            .reduce { acc, cur -> acc.and(cur) }
    }
    private fun parse(criteria: Criteria.Or<T>): R2DBCCriteria {
        if (criteria.value.isEmpty()) {
            return R2DBCCriteria.empty()
        }
        if (criteria.value.size == 1) {
            return parse(criteria.value[0])
        }
        return criteria.value
            .filter { it !is Criteria.Empty }
            .map { parse(it) }
            .reduce { acc, cur -> acc.or(cur) }
    }

    private fun parse(criteria: Criteria.Equals<T, *>): R2DBCCriteria {
        return R2DBCCriteria.where(columnName(criteria.key)).`is`(criteria.value)
    }
    private fun parse(criteria: Criteria.NotEquals<T, *>): R2DBCCriteria {
        return R2DBCCriteria.where(columnName(criteria.key)).not(criteria.value)
    }

    private fun parse(criteria: Criteria.Between<T, *>): R2DBCCriteria {
        return R2DBCCriteria.where(columnName(criteria.key)).between(criteria.value.start, criteria.value.endInclusive)
    }
    private fun parse(criteria: Criteria.NotBetween<T, *>): R2DBCCriteria {
        return R2DBCCriteria.where(columnName(criteria.key)).notBetween(criteria.value.start, criteria.value.endInclusive)
    }

    private fun parse(criteria: Criteria.LessThan<T, *>): R2DBCCriteria {
        return R2DBCCriteria.where(columnName(criteria.key)).lessThan(criteria.value)
    }
    private fun parse(criteria: Criteria.LessThanEquals<T, *>): R2DBCCriteria {
        return R2DBCCriteria.where(columnName(criteria.key)).lessThanOrEquals(criteria.value)
    }

    private fun parse(criteria: Criteria.GreaterThan<T, *>): R2DBCCriteria {
        return R2DBCCriteria.where(columnName(criteria.key)).greaterThan(criteria.value)
    }
    private fun parse(criteria: Criteria.GreaterThanEquals<T, *>): R2DBCCriteria {
        return R2DBCCriteria.where(columnName(criteria.key)).greaterThanOrEquals(criteria.value)
    }

    private fun parse(criteria: Criteria.IsNull<T, *>): R2DBCCriteria {
        return R2DBCCriteria.where(columnName(criteria.key)).isNull
    }
    private fun parse(criteria: Criteria.IsNotNull<T, *>): R2DBCCriteria {
        return R2DBCCriteria.where(columnName(criteria.key)).isNotNull
    }

    private fun parse(criteria: Criteria.Like<T, *>): R2DBCCriteria {
        return R2DBCCriteria.where(columnName(criteria.key)).like(criteria.value)
    }
    private fun parse(criteria: Criteria.NotLike<T, *>): R2DBCCriteria {
        return R2DBCCriteria.where(columnName(criteria.key)).notLike(criteria.value)
    }

    private fun parse(criteria: Criteria.Regexp<T, *>): R2DBCCriteria {
        throw RuntimeException()
    }
    private fun parse(criteria: Criteria.NotRegexp<T, *>): R2DBCCriteria {
        throw RuntimeException()
    }

    private fun parse(criteria: Criteria.In<T, *>): R2DBCCriteria {
        return R2DBCCriteria.where(columnName(criteria.key)).`in`(criteria.value)
    }
    private fun parse(criteria: Criteria.NotIn<T, *>): R2DBCCriteria {
        return R2DBCCriteria.where(columnName(criteria.key)).notIn(criteria.value)
    }

    private fun parse(criteria: Criteria.IsTrue<T, *>): R2DBCCriteria {
        return R2DBCCriteria.where(columnName(criteria.key)).isTrue
    }
    private fun parse(criteria: Criteria.IsFalse<T, *>): R2DBCCriteria {
        return R2DBCCriteria.where(columnName(criteria.key)).isFalse
    }
}
