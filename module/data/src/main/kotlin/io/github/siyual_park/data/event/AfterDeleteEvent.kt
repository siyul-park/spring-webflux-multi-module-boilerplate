package io.github.siyual_park.data.event

data class AfterDeleteEvent<T>(
    val entity: T
)
