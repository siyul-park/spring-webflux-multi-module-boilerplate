package io.github.siyual_park.data.expansion

import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.relational.core.mapping.Column
import kotlin.reflect.KProperty
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
