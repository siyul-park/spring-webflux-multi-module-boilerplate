package io.github.siyual_park.user.event.consumer

import io.github.siyual_park.auth.entity.ScopeTokenData
import io.github.siyual_park.data.event.BeforeDeleteEvent
import io.github.siyual_park.event.EventConsumer
import io.github.siyual_park.event.Subscribe
import io.github.siyual_park.user.repository.UserScopeDataRepository
import org.springframework.stereotype.Component

@Component
@Subscribe(filterBy = BeforeDeleteEvent::class)
class CascadeDeleteScopeToken(
    private val userScopeDataRepository: UserScopeDataRepository
) : EventConsumer<BeforeDeleteEvent<*>> {
    override suspend fun consume(event: BeforeDeleteEvent<*>) {
        val entity = event.entity as? ScopeTokenData ?: return
        userScopeDataRepository.deleteAllByScopeTokenId(entity.id)
    }
}
