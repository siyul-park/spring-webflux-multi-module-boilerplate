package io.github.siyual_park.event

import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class EventEmitter {
    private val listeners = mutableMapOf<KClass<out Event>, MutableList<EventListener<out Event>>>()

    suspend fun <E : Event> emit(event: E) {
        listeners[event::class]
            ?.filterIsInstance<EventListener<E>>()
            ?.forEach { it.onEvent(event) }
    }

    fun on(type: KClass<out Event>, listener: EventListener<*>) {
        val eventListeners = listeners.getOrPut(type) { mutableListOf() }
        eventListeners.add(listener)
    }
}
