package io.github.siyual_park.reader.sort

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.introspect.AnnotatedField
import io.github.siyual_park.data.expansion.columnName
import io.github.siyual_park.reader.exception.SortInvalidException
import org.springframework.data.domain.Sort
import java.net.URLDecoder
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

class SortParser<T : Any>(
    private val clazz: KClass<T>,
    private val objectMapper: ObjectMapper
) {
    private val regex = Regex("(\\w+)\\((\\w+)\\)")

    fun parseFromEncoded(sort: String): Sort {
        return parse(URLDecoder.decode(sort, "UTF-8"))
    }

    fun parse(sort: String): Sort {
        val result = regex.find(sort) ?: throw SortInvalidException()
        val (direction, property) = result.destructured

        val memberProperty = clazz.memberProperties.find {
            exportedPropertyName(it) == property
        } ?: throw SortInvalidException()

        return Sort.by(
            Sort.Direction.fromString(direction),
            columnName(memberProperty)
        )
    }

    private fun exportedPropertyName(property: KProperty<*>): String {
        return objectMapper.propertyNamingStrategy.nameForField(
            objectMapper.serializationConfig,
            AnnotatedField(null, property.javaField, null),
            property.name
        )
    }
}
