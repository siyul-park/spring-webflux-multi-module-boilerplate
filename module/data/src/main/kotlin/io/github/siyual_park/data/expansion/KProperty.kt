package io.github.siyual_park.data.expansion

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.relational.core.mapping.Column
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

fun <T> columnName(property: KProperty<T>): String {
    val column = property
        .javaField
        ?.annotations
        ?.filter { it is Column }
        ?.firstOrNull() as Column?

    return column?.value ?: property.name
}

fun <T> fieldName(property: KProperty<T>): String {
    val column = property
        .javaField
        ?.annotations
        ?.filter { it is Field }
        ?.firstOrNull() as Field?

    return column?.value ?: property.name
}

@Suppress("UNCHECKED_CAST")
fun <T : Any, ID : Any?> idProperty(clazz: KClass<T>): KProperty1<T, ID> {
    return (
        clazz.memberProperties.find { it.javaField?.annotations?.find { it is Id } != null }
            ?: throw RuntimeException()
        ) as KProperty1<T, ID>
}
