package io.github.siyual_park.data.repository.mongo

import io.github.siyual_park.data.Modifiable
import io.github.siyual_park.data.event.BeforeCreateEvent
import io.github.siyual_park.event.EventConsumer
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

class CreateTimestamp : EventConsumer<BeforeCreateEvent<*>> {
    override suspend fun consume(event: BeforeCreateEvent<*>) {
        val entity = event.entity

        if (entity !is Modifiable || !entity.javaClass.annotations.any { it is Document }) {
            return
        }

        entity.createdAt = Instant.now()
        entity.updatedAt = Instant.now()
    }
}
