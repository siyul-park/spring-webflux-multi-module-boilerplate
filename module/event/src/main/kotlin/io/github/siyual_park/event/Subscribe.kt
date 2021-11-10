package io.github.siyual_park.event

import kotlin.reflect.KClass

annotation class Subscribe(
    val type: KClass<out Event>
)
