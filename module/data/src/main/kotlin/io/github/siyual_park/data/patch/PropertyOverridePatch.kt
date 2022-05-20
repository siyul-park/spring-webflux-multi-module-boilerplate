package io.github.siyual_park.data.patch

import java.util.Optional
import javax.validation.ValidationException
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

@Suppress("UNCHECKED_CAST")
class PropertyOverridePatch<S : Any, T : Any>(
    private val value: T,
    target: KClass<T>,
    source: KClass<S>
) : Patch<S> {
    private val properties = mutableMapOf<KProperty1<T, Any?>, KMutableProperty1<S, Any?>>()

    init {
        target.memberProperties.forEach { targetProperty ->
            val updatedValue = targetProperty.get(value) as? Optional<Any?>? ?: return@forEach
            val sourceProperty = source.memberProperties.find { it.name == targetProperty.name } ?: throw IllegalArgumentException("${target.simpleName}.${targetProperty.name} is not exists.")
            if (sourceProperty !is KMutableProperty1) {
                throw IllegalArgumentException("${source.simpleName}.${sourceProperty.name} is must mutable.")
            }

            val targetReturnType = targetProperty.returnType
            if (!targetReturnType.isMarkedNullable || targetReturnType.classifier != Optional::class) {
                throw IllegalArgumentException("${target.simpleName}.${targetProperty.name} is must optional.")
            }

            if (!sourceProperty.returnType.isMarkedNullable && updatedValue.isEmpty) {
                throw ValidationException("${sourceProperty.name} is cannot be null")
            }

            properties[targetProperty] = sourceProperty as KMutableProperty1<S, Any?>
        }
    }

    override fun apply(entity: S): S {
        properties.forEach { (targetProperty, sourceProperty) ->
            val updatedValue = targetProperty.get(value) as? Optional<Any?>?
            updatedValue?.let { sourceProperty.set(entity, it.orElseGet { null }) }
        }

        return entity
    }

    companion object {
        inline fun <reified S : Any, reified T : Any> of(value: T) = PropertyOverridePatch(
            value,
            T::class,
            S::class,
        )
    }
}
