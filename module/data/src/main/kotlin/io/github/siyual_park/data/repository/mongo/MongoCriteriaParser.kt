package io.github.siyual_park.data.repository.mongo

import com.mongodb.BasicDBList
import io.github.siyual_park.data.criteria.Criteria
import io.github.siyual_park.data.criteria.CriteriaParser
import io.github.siyual_park.data.regexp.SqlLikeTranspiler
import org.springframework.data.mongodb.core.query.where
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import org.springframework.data.mongodb.core.query.Criteria as MongoCriteria

class MongoCriteriaParser<T : Any>(
    private val clazz: KClass<T>
) : CriteriaParser<MongoCriteria?> {
    override fun parse(criteria: Criteria): MongoCriteria? {
        return when (criteria) {
            is Criteria.Empty -> null
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

    private fun parse(criteria: Criteria.And): MongoCriteria? {
        val parsed = criteria.value.mapNotNull { parse(it) }
        if (parsed.isEmpty()) {
            return null
        }
        if (parsed.size == 1) {
            return parsed[0]
        }
        return MongoCriteria("\$and").`is`(createCriteriaList(parsed))
    }
    private fun parse(criteria: Criteria.Or): MongoCriteria? {
        val parsed = criteria.value.mapNotNull { parse(it) }
        if (parsed.isEmpty()) {
            return null
        }
        if (parsed.size == 1) {
            return parsed[0]
        }

        return MongoCriteria("\$or").`is`(createCriteriaList(parsed))
    }

    private fun parse(criteria: Criteria.Equals): MongoCriteria {
        return where(criteria.key).`is`(criteria.value)
    }
    private fun parse(criteria: Criteria.NotEquals): MongoCriteria {
        return where(criteria.key).ne(criteria.value)
    }

    private fun parse(criteria: Criteria.Between): MongoCriteria {
        return where(criteria.key).gte(criteria.value.start).lte(criteria.value.endInclusive)
    }
    private fun parse(criteria: Criteria.NotBetween): MongoCriteria {
        return MongoCriteria("\$or").`is`(
            createCriteriaList(listOf(where(criteria.key).lt(criteria.value.start), where(criteria.key).gt(criteria.value.endInclusive)))
        )
    }

    private fun parse(criteria: Criteria.LessThan): MongoCriteria {
        return where(criteria.key).lt(criteria.value)
    }
    private fun parse(criteria: Criteria.LessThanEquals): MongoCriteria {
        return where(criteria.key).lte(criteria.value)
    }

    private fun parse(criteria: Criteria.GreaterThan): MongoCriteria {
        return where(criteria.key).gt(criteria.value)
    }
    private fun parse(criteria: Criteria.GreaterThanEquals): MongoCriteria {
        return where(criteria.key).gte(criteria.value)
    }

    private fun parse(criteria: Criteria.IsNull): MongoCriteria {
        return where(criteria.key).isNull
    }
    private fun parse(criteria: Criteria.IsNotNull): MongoCriteria {
        return where(criteria.key).ne(null)
    }

    private fun parse(criteria: Criteria.Like): MongoCriteria {
        return where(criteria.key).regex(SqlLikeTranspiler.toRegEx(criteria.value))
    }
    private fun parse(criteria: Criteria.NotLike): MongoCriteria {
        return where(criteria.key).not().regex(SqlLikeTranspiler.toRegEx(criteria.value))
    }

    private fun parse(criteria: Criteria.Regexp): MongoCriteria {
        return where(criteria.key).regex(criteria.value)
    }
    private fun parse(criteria: Criteria.NotRegexp): MongoCriteria {
        return where(criteria.key).not().regex(criteria.value)
    }

    private fun parse(criteria: Criteria.In): MongoCriteria {
        return where(criteria.key).`in`(criteria.value)
    }
    private fun parse(criteria: Criteria.NotIn): MongoCriteria {
        return where(criteria.key).nin(criteria.value)
    }

    private fun parse(criteria: Criteria.IsTrue): MongoCriteria {
        return where(criteria.key).`is`(true)
    }
    private fun parse(criteria: Criteria.IsFalse): MongoCriteria {
        return where(criteria.key).`is`(false)
    }

    private fun createCriteriaList(criteria: Collection<MongoCriteria>): BasicDBList {
        val bsonList = BasicDBList()
        for (c in criteria) {
            bsonList.add(c.criteriaObject)
        }
        return bsonList
    }

    private fun where(key: String): MongoCriteria {
        return clazz.memberProperties.find { it.name == key }?.let { where(it) } ?: MongoCriteria.where(key)
    }
}
