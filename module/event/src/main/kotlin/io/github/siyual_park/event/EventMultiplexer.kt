package io.github.siyual_park.event

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter

class EventMultiplexer<E : Any> : EventConsumer<E> {
    private val consumers = mutableListOf<Pair<EventFilter, EventConsumer<E>>>()

    fun on(filter: EventFilter, consumer: EventConsumer<E>) {
        consumers.add(filter to consumer)
    }

    override suspend fun consume(event: E) {
        consumers.asFlow()
            .filter { (filter, _) -> filter.filter(event) }
            .collect { (_, consumer) -> consumer.consume(event) }
    }
}
