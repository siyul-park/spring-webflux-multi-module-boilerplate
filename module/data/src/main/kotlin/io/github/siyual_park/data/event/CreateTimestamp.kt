package io.github.siyual_park.data.event

import io.github.siyual_park.data.Modifiable
import io.github.siyual_park.event.EventConsumer
import java.time.Instant

class CreateTimestamp : EventConsumer<BeforeCreateEvent<*>> {
    override suspend fun consume(event: BeforeCreateEvent<*>) {
        val entity = event.entity

        if (entity !is Modifiable) {
            return
        }

        entity.createdAt = Instant.now()
        entity.updatedAt = Instant.now()
    }
}
