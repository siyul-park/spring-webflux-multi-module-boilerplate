package io.github.siyual_park.view

import kotlin.reflect.KClass

data class AccessLimiter<T>(
    val value: T,
    val level: KClass<*>? = null
)
