package io.github.siyual_park.data.expansion

import org.springframework.data.relational.core.mapping.Column
import kotlin.reflect.KProperty

fun <T> columnName(property: KProperty<T>): String {
    val column = property.annotations.filter { it is Column }
        .firstOrNull() as Column?

    return column?.value ?: property.name
}
