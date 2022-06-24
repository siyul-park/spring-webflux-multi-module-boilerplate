package io.github.siyual_park.presentation.filter

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.siyual_park.data.criteria.Criteria
import io.github.siyual_park.presentation.exception.FilterInvalidException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

@Suppress("UNCHECKED_CAST")
class RHSFilterParser<T : Any>(
    private val clazz: KClass<T>,
    private val objectMapper: ObjectMapper
) {
    private val regex = Regex("(.[^:]+):(.+)")

    fun parse(query: Map<KProperty1<T, *>, Collection<String?>?>): Criteria {
        try {
            var criteria = Criteria.And(listOf())
            query.forEach { (key, values) ->
                val property = clazz.memberProperties.find { it == key } ?: return@forEach
                val clazz = property.returnType.classifier as? KClass<*>
                    ?: throw FilterInvalidException("Can't find operand type.")

                values?.filterNotNull()?.forEach { value ->
                    val result = regex.find(value) ?: throw FilterInvalidException()
                    val (operator, operand) = result.destructured

                    val parsed = convert(operand, clazz)
                    criteria = create(property, operator, parsed).let { newone ->
                        criteria.copy(
                            criteria.value.toMutableList().also {
                                it.add(newone)
                            }
                        )
                    }
                }
            }

            if (criteria.value.isEmpty()) {
                return Criteria.Empty
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
            } catch (_: RuntimeException) {
            }
        }

        if (converted == null) {
            throw FilterInvalidException("Can't convert operand.")
        }

        return converted
    }

    private fun create(property: KProperty1<T, *>, operator: String, value: Any): Criteria {
        return when (operator) {
            "ne" -> Criteria.NotEquals(property.name, value as Any?)
            "eq" -> Criteria.Equals(property.name, value as Any?)
            "lk" -> Criteria.Like(property.name, value as String)
            "gt" -> Criteria.GreaterThan(property.name, value as Comparable<Any?>)
            "gte" -> Criteria.GreaterThanEquals(property.name, value as Comparable<Any?>)
            "lt" -> Criteria.LessThan(property.name, value as Comparable<Any?>)
            "lte" -> Criteria.LessThanEquals(property.name, value as Comparable<Any?>)
            else -> throw FilterInvalidException("Not support operator.")
        }
    }
}
