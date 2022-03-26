package io.github.siyual_park.data.event

import kotlin.reflect.KProperty1

data class BeforeUpdateEvent<T>(
    val entity: T,
    val diff: Map<KProperty1<T, *>, *>? = null
)
