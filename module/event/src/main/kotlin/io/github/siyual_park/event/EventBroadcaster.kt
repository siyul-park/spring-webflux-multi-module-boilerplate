package io.github.siyual_park.event

class EventBroadcaster : EventPublisher {
    private val eventPublishers = mutableListOf<EventPublisher>()

    fun use(eventPublisher: EventPublisher) {
        eventPublishers.add(eventPublisher)
    }

    override suspend fun <E : Any> publish(event: E) {
        eventPublishers.forEach {
            it.publish(event)
        }
    }
}
