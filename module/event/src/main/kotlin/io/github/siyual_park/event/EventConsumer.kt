package io.github.siyual_park.event

interface EventConsumer<E : Any> {
    suspend fun consume(event: E)
}
