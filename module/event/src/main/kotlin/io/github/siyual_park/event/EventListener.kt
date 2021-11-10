package io.github.siyual_park.event

interface EventListener<E : Event> {
    suspend fun onEvent(event: E)
}
