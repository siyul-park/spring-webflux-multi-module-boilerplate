package io.github.siyual_park.data.event

data class BeforeDeleteEvent<T>(
    val entity: T
)
