package io.github.siyual_park.data.repository.mongo

import io.github.siyual_park.data.Modifiable
import io.github.siyual_park.data.event.BeforeUpdateEvent
import io.github.siyual_park.event.EventConsumer
import io.github.siyual_park.event.Subscribe
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.stereotype.Component
import java.time.Instant
import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST")
@Component
@Subscribe(filterBy = BeforeUpdateEvent::class)
class UpdateTimestamp : EventConsumer<BeforeUpdateEvent<*>> {
    override suspend fun consume(event: BeforeUpdateEvent<*>) {
        val entity = event.entity
        val diff = event.diff

        if (entity !is Modifiable || !entity.javaClass.annotations.any { it is Document }) {
            return
        }
        if (diff == null) {
            return
        }

        (diff as MutableMap<KProperty1<*, *>, Any>)[Modifiable::updatedAt] = Instant.now()
    }
}
