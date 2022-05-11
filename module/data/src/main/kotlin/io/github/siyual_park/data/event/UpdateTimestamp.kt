package io.github.siyual_park.data.event

import io.github.siyual_park.data.Modifiable
import io.github.siyual_park.event.EventConsumer
import java.time.Instant
import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST")
class UpdateTimestamp : EventConsumer<BeforeUpdateEvent<*>> {
    override suspend fun consume(event: BeforeUpdateEvent<*>) {
        val entity = event.entity
        val diff = event.diff

        if (entity !is Modifiable) {
            return
        }
        if (diff == null) {
            return
        }

        (diff as MutableMap<KProperty1<*, *>, Any>)[Modifiable::updatedAt] = Instant.now()
    }
}
