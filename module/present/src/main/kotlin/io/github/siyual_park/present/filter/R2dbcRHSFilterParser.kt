package io.github.siyual_park.present.filter

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.siyual_park.present.exception.FilterInvalidException
import org.springframework.data.relational.core.query.Criteria
import kotlin.reflect.KClass

class R2dbcRHSFilterParser<T : Any>(
    clazz: KClass<T>,
    objectMapper: ObjectMapper
) : RHSFilterParser<T, Criteria>(clazz, objectMapper) {
    override fun empty(): Criteria {
        return Criteria.empty()
    }

    override fun and(x: Criteria, y: Criteria): Criteria {
        return x.and(y)
    }

    override fun create(columnName: String, operator: String, value: Any): Criteria {
        val step = Criteria.where(columnName)
        return when (operator) {
            "ne" -> step.not(value)
            "eq" -> step.`is`(value)
            "lk" -> step.like(value)
            "gt" -> step.greaterThan(value)
            "gte" -> step.greaterThanOrEquals(value)
            "lt" -> step.lessThan(value)
            "lte" -> step.lessThanOrEquals(value)
            else -> throw FilterInvalidException("Not support operator.")
        }
    }
}
