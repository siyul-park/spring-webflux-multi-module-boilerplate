package io.github.siyual_park.user.event.consumer

import io.github.siyual_park.data.event.AfterUpdateEvent
import io.github.siyual_park.event.EventConsumer
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.event.Subscribe
import io.github.siyual_park.user.domain.UserStorage
import io.github.siyual_park.user.entity.UserData
import io.github.siyual_park.user.event.RequestActivateEvent
import org.springframework.stereotype.Component

@Component
@Subscribe(filterBy = AfterUpdateEvent::class)
class UpdateActivatedAt(
    private val eventPublisher: EventPublisher,
    private val userStorage: UserStorage
) : EventConsumer<AfterUpdateEvent<*>> {
    override suspend fun consume(event: AfterUpdateEvent<*>) {
        val entity = event.entity as? UserData ?: return
        val diff = event.diff ?: return

        if (!diff.containsKey(UserData::activatedAt) || diff[UserData::activatedAt] != null) {
            return
        }

        val id = entity.id ?: return
        val user = userStorage.load(id) ?: return

        eventPublisher.publish(RequestActivateEvent(user))
    }
}
