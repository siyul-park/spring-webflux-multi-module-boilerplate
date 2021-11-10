package io.github.siyual_park.event

import kotlin.reflect.KClass

annotation class Subscribe(
    val filterBy: KClass<*>
)
