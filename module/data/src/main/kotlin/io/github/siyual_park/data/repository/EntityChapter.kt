package io.github.siyual_park.data.repository

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class EntityChapter<T : Any>(
    private val clazz: KClass<T>
) {
    fun snapshot(entity: T): Map<KProperty1<T, *>, Any?> {
        val snapshot = mutableMapOf<KProperty1<T, *>, Any?>()
        clazz.memberProperties.forEach {
            snapshot[it] = it.get(entity)
        }
        return snapshot
    }

    fun diff(source: Map<KProperty1<T, *>, Any?>, target: Map<KProperty1<T, *>, Any?>): Map<KProperty1<T, *>, Any?> {
        val propertyDiff = mutableMapOf<KProperty1<T, *>, Any?>()
        clazz.memberProperties.forEach {
            val sourceValue = source[it]
            val targetValue = target[it]

            if (sourceValue != targetValue) {
                propertyDiff[it] = targetValue
            }
        }

        return propertyDiff
    }
}
