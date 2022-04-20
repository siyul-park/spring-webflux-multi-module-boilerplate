package io.github.siyual_park.search.filter

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.siyual_park.data.expansion.columnName
import io.github.siyual_park.search.exception.FilterInvalidException
import org.springframework.data.relational.core.query.Criteria
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

abstract class RHSFilterParser<T : Any, C: Any>(
    private val clazz: KClass<T>,
    private val objectMapper: ObjectMapper
) {
    private val regex = Regex("(.[^:]+):(.+)")

    fun parse(query: Map<KProperty1<T, *>, Collection<String?>>): C {
        try {
            var criteria = empty()
            query.forEach { (key, values) ->
                val property = clazz.memberProperties.find { it == key } ?: return@forEach
                val columnName = columnName(property)
                val clazz = property.returnType.classifier as? KClass<*>
                    ?: throw FilterInvalidException("Can't find operand type.")

                values.filterNotNull().forEach { value ->
                    val result = regex.find(value) ?: throw FilterInvalidException()
                    val (operator, operand) = result.destructured

                    val parsed = convert(operand, clazz)
                    criteria = and(
                        criteria,
                        create(
                            columnName,
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
            } catch (_: RuntimeException) {
            }
        }

        if (converted == null) {
            throw FilterInvalidException("Can't convert operand.")
        }

        return converted
    }

    protected abstract fun empty(): C
    protected abstract fun and(x: C, y: C): C
    protected abstract fun create(columnName: String, operator: String, value: Any): C
}
