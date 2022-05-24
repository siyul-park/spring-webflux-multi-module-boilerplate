package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.criteria.Criteria
import io.github.siyual_park.data.criteria.CriteriaParser
import io.github.siyual_park.data.expansion.columnName
import org.springframework.data.relational.core.query.CriteriaDefinition
import org.springframework.data.relational.core.query.Criteria as R2DBCCriteria

class R2DBCCriteriaParser<T : Any> : CriteriaParser<T, CriteriaDefinition> {
    override fun parse(criteria: Criteria<T>): CriteriaDefinition {
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
            is Criteria.Like -> parse(criteria)
            is Criteria.NotLike -> parse(criteria)
            is Criteria.In<T, *> -> parse(criteria)
            is Criteria.NotIn<T, *> -> parse(criteria)
            is Criteria.IsTrue -> parse(criteria)
            is Criteria.IsFalse -> parse(criteria)
        }
    }

    private fun parse(criteria: Criteria.And<T>): CriteriaDefinition {
        if (criteria.value.isEmpty()) {
            return R2DBCCriteria.empty()
        }
        if (criteria.value.size == 1) {
            return parse(criteria.value[0])
        }
        return R2DBCCriteria.empty().and(criteria.value.map { parse(it) })
    }
    private fun parse(criteria: Criteria.Or<T>): CriteriaDefinition {
        if (criteria.value.isEmpty()) {
            return R2DBCCriteria.empty()
        }
        if (criteria.value.size == 1) {
            return parse(criteria.value[0])
        }
        return R2DBCCriteria.empty().or(criteria.value.map { parse(it) })
    }

    private fun parse(criteria: Criteria.Equals<T, *>): CriteriaDefinition {
        return R2DBCCriteria.where(columnName(criteria.key)).`is`(criteria.value)
    }
    private fun parse(criteria: Criteria.NotEquals<T, *>): CriteriaDefinition {
        return R2DBCCriteria.where(columnName(criteria.key)).not(criteria.value)
    }

    private fun parse(criteria: Criteria.Between<T, *>): CriteriaDefinition {
        return R2DBCCriteria.where(columnName(criteria.key)).between(criteria.value.start, criteria.value.endInclusive)
    }
    private fun parse(criteria: Criteria.NotBetween<T, *>): CriteriaDefinition {
        return R2DBCCriteria.where(columnName(criteria.key)).notBetween(criteria.value.start, criteria.value.endInclusive)
    }

    private fun parse(criteria: Criteria.LessThan<T, *>): CriteriaDefinition {
        return R2DBCCriteria.where(columnName(criteria.key)).lessThan(criteria.value)
    }
    private fun parse(criteria: Criteria.LessThanEquals<T, *>): CriteriaDefinition {
        return R2DBCCriteria.where(columnName(criteria.key)).lessThanOrEquals(criteria.value)
    }

    private fun parse(criteria: Criteria.GreaterThan<T, *>): CriteriaDefinition {
        return R2DBCCriteria.where(columnName(criteria.key)).greaterThan(criteria.value)
    }
    private fun parse(criteria: Criteria.GreaterThanEquals<T, *>): CriteriaDefinition {
        return R2DBCCriteria.where(columnName(criteria.key)).greaterThanOrEquals(criteria.value)
    }

    private fun parse(criteria: Criteria.IsNull<T, *>): CriteriaDefinition {
        return R2DBCCriteria.where(columnName(criteria.key)).isNull
    }
    private fun parse(criteria: Criteria.IsNotNull<T, *>): CriteriaDefinition {
        return R2DBCCriteria.where(columnName(criteria.key)).isNotNull
    }

    private fun parse(criteria: Criteria.Like<T>): CriteriaDefinition {
        return R2DBCCriteria.where(columnName(criteria.key)).like(criteria.value)
    }
    private fun parse(criteria: Criteria.NotLike<T>): CriteriaDefinition {
        return R2DBCCriteria.where(columnName(criteria.key)).notLike(criteria.value)
    }

    private fun parse(criteria: Criteria.In<T, *>): CriteriaDefinition {
        return R2DBCCriteria.where(columnName(criteria.key)).`in`(criteria.value)
    }
    private fun parse(criteria: Criteria.NotIn<T, *>): CriteriaDefinition {
        return R2DBCCriteria.where(columnName(criteria.key)).notIn(criteria.value)
    }

    private fun parse(criteria: Criteria.IsTrue<T>): CriteriaDefinition {
        return R2DBCCriteria.where(columnName(criteria.key)).isTrue
    }
    private fun parse(criteria: Criteria.IsFalse<T>): CriteriaDefinition {
        return R2DBCCriteria.where(columnName(criteria.key)).isFalse
    }
}
