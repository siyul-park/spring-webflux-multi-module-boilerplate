package io.github.siyual_park.data.event

data class BeforeCreateEvent<T>(
    val entity: T
)
