package io.github.siyual_park.data.event

data class AfterSaveEvent<T>(
    val entity: T
)
