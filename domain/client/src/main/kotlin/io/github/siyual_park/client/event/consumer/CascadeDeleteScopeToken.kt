package io.github.siyual_park.client.event.consumer

import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.client.repository.ClientScopeRepository
import io.github.siyual_park.data.event.BeforeDeleteEvent
import io.github.siyual_park.event.EventConsumer
import io.github.siyual_park.event.Subscribe
import org.springframework.stereotype.Component

@Component
@Subscribe(filterBy = BeforeDeleteEvent::class)
class CascadeDeleteScopeToken(
    private val clientScopeRepository: ClientScopeRepository
) : EventConsumer<BeforeDeleteEvent<*>> {
    override suspend fun consume(event: BeforeDeleteEvent<*>) {
        val entity = event.entity as? ScopeToken ?: return
        clientScopeRepository.deleteAllByScopeTokenId(entity.id)
    }
}
