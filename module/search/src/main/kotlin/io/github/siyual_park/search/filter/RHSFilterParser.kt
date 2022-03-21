package io.github.siyual_park.search.filter

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.siyual_park.data.expansion.columnName
import io.github.siyual_park.search.exception.FilterInvalidException
import org.springframework.data.relational.core.query.Criteria
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties

class RHSFilterParser<T : Any>(
    private val clazz: KClass<T>,
    private val objectMapper: ObjectMapper
) {
    private val regex = Regex("(.+):(.+)")

    fun parseFromProperty(query: Map<KProperty<*>, Collection<String?>>): Criteria {
        return parse(query.mapKeys { it.key.name })
    }

    fun parse(query: Map<String, Collection<String?>>): Criteria {
        try {
            var criteria = Criteria.empty()

            query.forEach { (key, values) ->
                val property = clazz.memberProperties.find { it.name == key } ?: return@forEach
                val columnName = columnName(property)
                val clazz = property.returnType.classifier as? KClass<*>
                    ?: throw FilterInvalidException("Can't find operand type.")

                values.filterNotNull().forEach { value ->
                    val result = regex.find(value) ?: throw FilterInvalidException()
                    val (operator, operand) = result.destructured

                    val parsed = convert(operand, clazz)
                    criteria = criteria.and(
                        createCriteria(
                            Criteria.where(columnName),
                            operator,
                            parsed
                        )
                    )
                }
            }

            return criteria
        } catch (e: Exception) {
            throw FilterInvalidException(e.message)
        }
    }

    private fun convert(operand: String, clazz: KClass<*>): Any {
        val candidates = listOfNotNull(
            operand,
            operand.toIntOrNull(),
            operand.toLongOrNull(),
            operand.toBooleanStrictOrNull()
        )
        var converted: Any? = null
        for (candidate in candidates) {
            try {
                converted = objectMapper.convertValue(candidate, clazz.java)
                break
            } catch (e: RuntimeException) {
            }
        }

        if (converted == null) {
            throw FilterInvalidException("Can't convert operand.")
        }

        return converted
    }

    private fun createCriteria(step: Criteria.CriteriaStep, operator: String, value: Any): Criteria {
        return when (operator) {
            "ne" -> step.not(value)
            "eq" -> step.`is`(value)
            "like" -> step.like(value)
            "gt" -> step.greaterThan(value)
            "gte" -> step.greaterThanOrEquals(value)
            "lt" -> step.lessThan(value)
            "lte" -> step.lessThanOrEquals(value)
            else -> throw FilterInvalidException("Not support operator.")
        }
    }
}
