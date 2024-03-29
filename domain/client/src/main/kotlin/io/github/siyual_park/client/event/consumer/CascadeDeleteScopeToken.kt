package io.github.siyual_park.client.event.consumer

import io.github.siyual_park.auth.entity.ScopeTokenEntity
import io.github.siyual_park.client.repository.ClientScopeEntityRepository
import io.github.siyual_park.data.event.BeforeDeleteEvent
import io.github.siyual_park.event.EventConsumer
import io.github.siyual_park.event.Subscribe
import org.springframework.stereotype.Component

@Component
@Subscribe(filterBy = BeforeDeleteEvent::class)
class CascadeDeleteScopeToken(
    private val clientScopeEntityRepository: ClientScopeEntityRepository
) : EventConsumer<BeforeDeleteEvent<*>> {
    override suspend fun consume(event: BeforeDeleteEvent<*>) {
        val entity = event.entity as? ScopeTokenEntity ?: return
        clientScopeEntityRepository.deleteAllByScopeTokenId(entity.id)
    }
}
