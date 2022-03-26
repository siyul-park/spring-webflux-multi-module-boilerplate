package io.github.siyual_park.user.event.consumer

import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.data.event.BeforeDeleteEvent
import io.github.siyual_park.event.EventConsumer
import io.github.siyual_park.event.Subscribe
import io.github.siyual_park.user.repository.UserScopeRepository
import org.springframework.stereotype.Component

@Component
@Subscribe(filterBy = BeforeDeleteEvent::class)
class CascadeDeleteScopeToken(
    private val userScopeRepository: UserScopeRepository
) : EventConsumer<BeforeDeleteEvent<*>> {
    override suspend fun consume(event: BeforeDeleteEvent<*>) {
        val entity = event.entity as? ScopeToken ?: return
        userScopeRepository.deleteAllByScopeTokenId(entity.id)
    }
}
