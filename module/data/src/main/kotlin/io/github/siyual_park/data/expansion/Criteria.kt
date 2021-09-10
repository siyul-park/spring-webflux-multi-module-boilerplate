package io.github.siyual_park.data.expansion

import org.springframework.data.relational.core.query.Criteria
import kotlin.reflect.KProperty

fun <T> where(property: KProperty<T>): Criteria.CriteriaStep {
    return Criteria.where(columnName(property))
}
