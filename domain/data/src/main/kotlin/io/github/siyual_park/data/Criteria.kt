package io.github.siyual_park.data

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.query.Criteria
import kotlin.reflect.KProperty

fun <T> where(property: KProperty<T>): Criteria.CriteriaStep {
    val column = property.annotations.filter { it is Column }
        .firstOrNull() as Column?

    return Criteria.where(column?.value ?: property.name)
}
