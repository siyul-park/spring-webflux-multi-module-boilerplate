package io.github.siyual_park.data.event

data class AfterCreateEvent<T>(
    val entity: T
)
