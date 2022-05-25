package io.github.siyual_park.data.repository.mongo

import com.mongodb.BasicDBList
import io.github.siyual_park.data.criteria.Criteria
import io.github.siyual_park.data.criteria.CriteriaParser
import io.github.siyual_park.data.regexp.SqlLikeTranspiler
import org.springframework.data.mongodb.core.query.where
import org.springframework.data.mongodb.core.query.Criteria as MongoCriteria

class MongoCriteriaParser<T : Any> : CriteriaParser<T, MongoCriteria?> {
    override fun parse(criteria: Criteria<T>): MongoCriteria? {
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

    private fun parse(criteria: Criteria.And<T>): MongoCriteria? {
        val parsed = criteria.value.mapNotNull { parse(it) }
        if (parsed.isEmpty()) {
            return null
        }
        if (parsed.size == 1) {
            return parsed[0]
        }
        return MongoCriteria("\$and").`is`(createCriteriaList(parsed))
    }
    private fun parse(criteria: Criteria.Or<T>): MongoCriteria? {
        val parsed = criteria.value.mapNotNull { parse(it) }
        if (parsed.isEmpty()) {
            return null
        }
        if (parsed.size == 1) {
            return parsed[0]
        }

        return MongoCriteria("\$or").`is`(createCriteriaList(parsed))
    }

    private fun parse(criteria: Criteria.Equals<T, *>): MongoCriteria {
        return where(criteria.key).`is`(criteria.value)
    }
    private fun parse(criteria: Criteria.NotEquals<T, *>): MongoCriteria {
        return where(criteria.key).ne(criteria.value)
    }

    private fun parse(criteria: Criteria.Between<T, *>): MongoCriteria {
        return where(criteria.key).gte(criteria.value.start).lte(criteria.value.endInclusive)
    }
    private fun parse(criteria: Criteria.NotBetween<T, *>): MongoCriteria {
        return MongoCriteria("\$or").`is`(
            createCriteriaList(listOf(where(criteria.key).lt(criteria.value.start), where(criteria.key).gt(criteria.value.endInclusive)))
        )
    }

    private fun parse(criteria: Criteria.LessThan<T, *>): MongoCriteria {
        return where(criteria.key).lt(criteria.value)
    }
    private fun parse(criteria: Criteria.LessThanEquals<T, *>): MongoCriteria {
        return where(criteria.key).lte(criteria.value)
    }

    private fun parse(criteria: Criteria.GreaterThan<T, *>): MongoCriteria {
        return where(criteria.key).gt(criteria.value)
    }
    private fun parse(criteria: Criteria.GreaterThanEquals<T, *>): MongoCriteria {
        return where(criteria.key).gte(criteria.value)
    }

    private fun parse(criteria: Criteria.IsNull<T, *>): MongoCriteria {
        return where(criteria.key).isNull
    }
    private fun parse(criteria: Criteria.IsNotNull<T, *>): MongoCriteria {
        return where(criteria.key).ne(null)
    }

    private fun parse(criteria: Criteria.Like<T, *>): MongoCriteria {
        return where(criteria.key).regex(SqlLikeTranspiler.toRegEx(criteria.value))
    }
    private fun parse(criteria: Criteria.NotLike<T, *>): MongoCriteria {
        return where(criteria.key).not().regex(SqlLikeTranspiler.toRegEx(criteria.value))
    }

    private fun parse(criteria: Criteria.Regexp<T, *>): MongoCriteria {
        return where(criteria.key).regex(criteria.value)
    }
    private fun parse(criteria: Criteria.NotRegexp<T, *>): MongoCriteria {
        return where(criteria.key).not().regex(criteria.value)
    }

    private fun parse(criteria: Criteria.In<T, *>): MongoCriteria {
        return where(criteria.key).`in`(criteria.value)
    }
    private fun parse(criteria: Criteria.NotIn<T, *>): MongoCriteria {
        return where(criteria.key).nin(criteria.value)
    }

    private fun parse(criteria: Criteria.IsTrue<T, *>): MongoCriteria {
        return where(criteria.key).`is`(true)
    }
    private fun parse(criteria: Criteria.IsFalse<T, *>): MongoCriteria {
        return where(criteria.key).`is`(false)
    }

    private fun createCriteriaList(criteria: Collection<MongoCriteria>): BasicDBList {
        val bsonList = BasicDBList()
        for (c in criteria) {
            bsonList.add(c.criteriaObject)
        }
        return bsonList
    }
}
