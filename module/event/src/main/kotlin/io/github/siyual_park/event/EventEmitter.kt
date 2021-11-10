package io.github.siyual_park.event

import org.springframework.stereotype.Component

@Suppress("UNCHECKED_CAST")
@Component
class EventEmitter : EventPublisher {
    private val eventMultiplexer = EventMultiplexer<Any>()

    fun on(filter: EventFilter, consumer: EventConsumer<*>) {
        eventMultiplexer.on(filter, consumer as EventConsumer<Any>)
    }

    override suspend fun <E : Any> publish(event: E) {
        eventMultiplexer.consume(event)
    }
}
