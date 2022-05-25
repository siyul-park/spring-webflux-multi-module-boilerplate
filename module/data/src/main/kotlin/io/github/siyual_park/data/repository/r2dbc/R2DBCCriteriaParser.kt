package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.criteria.Criteria
import io.github.siyual_park.data.criteria.CriteriaParser
import io.github.siyual_park.data.expansion.columnName
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import org.springframework.data.relational.core.query.Criteria as R2DBCCriteria
import org.springframework.data.relational.core.query.Criteria.CriteriaStep as R2DBCCriteriaStep

class R2DBCCriteriaParser<T : Any>(
    clazz: KClass<T>
) : CriteriaParser<R2DBCCriteria> {
    private val columnNames = clazz.memberProperties.associate { it.name to columnName(it) }

    override fun parse(criteria: Criteria): R2DBCCriteria {
        return when (criteria) {
            is Criteria.Empty -> R2DBCCriteria.empty()
            is Criteria.And -> parse(criteria)
            is Criteria.Or -> parse(criteria)
            is Criteria.Equals -> parse(criteria)
            is Criteria.NotEquals -> parse(criteria)
            is Criteria.Between -> parse(criteria)
            is Criteria.NotBetween -> parse(criteria)
            is Criteria.LessThan -> parse(criteria)
            is Criteria.LessThanEquals -> parse(criteria)
            is Criteria.GreaterThan -> parse(criteria)
            is Criteria.GreaterThanEquals -> parse(criteria)
            is Criteria.IsNull -> parse(criteria)
            is Criteria.IsNotNull -> parse(criteria)
            is Criteria.Like -> parse(criteria)
            is Criteria.NotLike -> parse(criteria)
            is Criteria.Regexp -> parse(criteria)
            is Criteria.NotRegexp -> parse(criteria)
            is Criteria.In -> parse(criteria)
            is Criteria.NotIn -> parse(criteria)
            is Criteria.IsTrue -> parse(criteria)
            is Criteria.IsFalse -> parse(criteria)
        }
    }

    private fun parse(criteria: Criteria.And): R2DBCCriteria {
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
    private fun parse(criteria: Criteria.Or): R2DBCCriteria {
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

    private fun parse(criteria: Criteria.Equals): R2DBCCriteria {
        if (criteria.value == null) {
            return where(criteria.key).isNull
        }
        return where(criteria.key).`is`(criteria.value)
    }
    private fun parse(criteria: Criteria.NotEquals): R2DBCCriteria {
        if (criteria.value == null) {
            return where(criteria.key).isNotNull
        }
        return where(criteria.key).not(criteria.value)
    }

    private fun parse(criteria: Criteria.Between): R2DBCCriteria {
        return where(criteria.key).between(criteria.value.start, criteria.value.endInclusive)
    }
    private fun parse(criteria: Criteria.NotBetween): R2DBCCriteria {
        return where(criteria.key).notBetween(criteria.value.start, criteria.value.endInclusive)
    }

    private fun parse(criteria: Criteria.LessThan): R2DBCCriteria {
        return where(criteria.key).lessThan(criteria.value)
    }
    private fun parse(criteria: Criteria.LessThanEquals): R2DBCCriteria {
        return where(criteria.key).lessThanOrEquals(criteria.value)
    }

    private fun parse(criteria: Criteria.GreaterThan): R2DBCCriteria {
        return where(criteria.key).greaterThan(criteria.value)
    }
    private fun parse(criteria: Criteria.GreaterThanEquals): R2DBCCriteria {
        return where(criteria.key).greaterThanOrEquals(criteria.value)
    }

    private fun parse(criteria: Criteria.IsNull): R2DBCCriteria {
        return where(criteria.key).isNull
    }
    private fun parse(criteria: Criteria.IsNotNull): R2DBCCriteria {
        return where(criteria.key).isNotNull
    }

    private fun parse(criteria: Criteria.Like): R2DBCCriteria {
        return where(criteria.key).like(criteria.value)
    }
    private fun parse(criteria: Criteria.NotLike): R2DBCCriteria {
        return where(criteria.key).notLike(criteria.value)
    }

    private fun parse(criteria: Criteria.Regexp): R2DBCCriteria {
        throw RuntimeException()
    }
    private fun parse(criteria: Criteria.NotRegexp): R2DBCCriteria {
        throw RuntimeException()
    }

    private fun parse(criteria: Criteria.In): R2DBCCriteria {
        return where(criteria.key).`in`(criteria.value)
    }
    private fun parse(criteria: Criteria.NotIn): R2DBCCriteria {
        return where(criteria.key).notIn(criteria.value)
    }

    private fun parse(criteria: Criteria.IsTrue): R2DBCCriteria {
        return where(criteria.key).isTrue
    }
    private fun parse(criteria: Criteria.IsFalse): R2DBCCriteria {
        return where(criteria.key).isFalse
    }

    private fun where(key: String): R2DBCCriteriaStep {
        return R2DBCCriteria.where(columnNames[key] ?: throw IllegalArgumentException("$key is invalid}"))
    }
}
