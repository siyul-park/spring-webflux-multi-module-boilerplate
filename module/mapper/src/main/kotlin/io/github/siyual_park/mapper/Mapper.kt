package io.github.siyual_park.mapper

import kotlin.reflect.KClass

interface Mapper<SOURCE : Any, TARGET : Any> {
    val sourceClazz: KClass<SOURCE>
    val targetClazz: KClass<TARGET>

    suspend fun map(source: SOURCE): TARGET
}
