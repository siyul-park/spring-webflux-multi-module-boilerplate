package io.github.siyual_park.presentation.project

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.introspect.AnnotatedField
import io.github.siyual_park.presentation.exception.ProjectionInvalidException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

class ProjectionParser<T : Any>(
    private val clazz: KClass<T>,
    private val objectMapper: ObjectMapper
) {
    fun parse(projection: Collection<String>?): ProjectNode {
        return convert(ProjectNode.from(projection ?: listOf()), clazz)
    }

    private fun convert(node: ProjectNode, clazz: KClass<*>): ProjectNode {
        if (node is ProjectNode.Leaf) {
            return node
        }
        node as ProjectNode.Stem

        val converted = ProjectNode.Stem()
        node.forEach { (key, value) ->
            val property = clazz.memberProperties.find {
                exportedPropertyName(it) == key
            } ?: throw ProjectionInvalidException()

            converted[property.name] = convert(value, property.returnType.classifier as KClass<*>)
        }
        return converted
    }

    private fun exportedPropertyName(property: KProperty<*>): String {
        return objectMapper.propertyNamingStrategy?.nameForField(
            objectMapper.serializationConfig,
            AnnotatedField(null, property.javaField, null),
            property.name
        ) ?: property.name
    }
}
