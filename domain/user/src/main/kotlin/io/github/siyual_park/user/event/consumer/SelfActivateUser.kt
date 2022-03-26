package io.github.siyual_park.user.event.consumer

import io.github.siyual_park.event.EventConsumer
import io.github.siyual_park.event.Subscribe
import io.github.siyual_park.user.event.RequestActivateEvent
import org.springframework.stereotype.Component

@Component
@Subscribe(filterBy = RequestActivateEvent::class)
class SelfActivateUser : EventConsumer<RequestActivateEvent> {
    override suspend fun consume(event: RequestActivateEvent) {
        // TODO(verification email)
        event.entity.activate()
        event.entity.sync()
    }
}
