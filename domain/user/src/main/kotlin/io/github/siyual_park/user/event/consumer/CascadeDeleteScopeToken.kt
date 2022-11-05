package io.github.siyual_park.user.event.consumer

import io.github.siyual_park.auth.entity.ScopeTokenEntity
import io.github.siyual_park.data.event.BeforeDeleteEvent
import io.github.siyual_park.event.EventConsumer
import io.github.siyual_park.event.Subscribe
import io.github.siyual_park.user.repository.UserScopeEntityRepository
import org.springframework.stereotype.Component

@Component
@Subscribe(filterBy = BeforeDeleteEvent::class)
class CascadeDeleteScopeToken(
    private val userScopeEntityRepository: UserScopeEntityRepository
) : EventConsumer<BeforeDeleteEvent<*>> {
    override suspend fun consume(event: BeforeDeleteEvent<*>) {
        val entity = event.entity as? ScopeTokenEntity ?: return
        userScopeEntityRepository.deleteAllByScopeTokenId(entity.id)
    }
}
