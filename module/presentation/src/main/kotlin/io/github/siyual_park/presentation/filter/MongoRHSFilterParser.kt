package io.github.siyual_park.presentation.filter

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.siyual_park.presentation.exception.FilterInvalidException
import org.springframework.data.mongodb.core.query.Criteria
import kotlin.reflect.KClass

class MongoRHSFilterParser<T : Any>(
    clazz: KClass<T>,
    objectMapper: ObjectMapper
) : RHSFilterParser<T, Criteria?>(clazz, objectMapper) {
    override fun empty(): Criteria? {
        return null
    }

    override fun and(x: Criteria?, y: Criteria?): Criteria? {
        if (x === null && y === null) {
            return null
        }
        if (x === null) {
            return y
        }
        if (y === null) {
            return x
        }

        return x.andOperator(y)
    }

    override fun create(columnName: String, operator: String, value: Any): Criteria {
        val step = Criteria.where(columnName)
        return when (operator) {
            "ne" -> step.ne(value)
            "eq" -> step.`is`(value)
            "lk" -> step.regex(value.toString())
            "gt" -> step.gt(value)
            "gte" -> step.gte(value)
            "lt" -> step.lt(value)
            "lte" -> step.lte(value)
            else -> throw FilterInvalidException("Not support operator.")
        }
    }
}
