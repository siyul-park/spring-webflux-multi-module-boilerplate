package io.github.siyual_park.event

interface EventPublisher {
    suspend fun <E : Any> publish(event: E)
}
