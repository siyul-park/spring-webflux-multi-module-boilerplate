package io.github.siyual_park.presentation.sort

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.introspect.AnnotatedField
import io.github.siyual_park.data.expansion.columnName
import io.github.siyual_park.presentation.exception.SortInvalidException
import org.springframework.data.domain.Sort
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

class SortParser<T : Any>(
    private val clazz: KClass<T>,
    private val objectMapper: ObjectMapper
) {
    private val regex = Regex("(.[^:]+):(.+)")

    fun parse(sort: Collection<String>): Sort {
        return sort
            .map { parse(it) }
            .reduce { acc, cur -> acc.and(cur) }
    }

    fun parse(sort: String): Sort {
        try {
            val result = regex.find(sort) ?: throw SortInvalidException()
            val (direction, property) = result.destructured

            val memberProperty = clazz.memberProperties.find {
                exportedPropertyName(it) == property
            } ?: throw SortInvalidException()

            return Sort.by(
                Sort.Direction.fromString(direction),
                columnName(memberProperty)
            )
        } catch (e: IllegalArgumentException) {
            throw SortInvalidException(e.message, e)
        }
    }

    private fun exportedPropertyName(property: KProperty<*>): String {
        return objectMapper.propertyNamingStrategy?.nameForField(
            objectMapper.serializationConfig,
            AnnotatedField(null, property.javaField, null),
            property.name
        ) ?: property.name
    }
}
