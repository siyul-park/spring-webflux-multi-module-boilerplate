package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.client.configuration.RootClientProperty
import io.github.siyual_park.client.entity.ClientScope
import io.github.siyual_park.client.repository.ClientScopeRepository
import io.github.siyual_park.data.event.AfterSaveEvent
import io.github.siyual_park.event.EventConsumer
import io.github.siyual_park.event.Subscribe
import org.springframework.stereotype.Component

@Component
@Subscribe(filterBy = AfterSaveEvent::class)
class SyncRootClientScope(
    private val clientFinder: ClientFinder,
    private val clientScopeRepository: ClientScopeRepository,
    private val property: RootClientProperty,
) : EventConsumer<AfterSaveEvent<*>> {
    override suspend fun consume(event: AfterSaveEvent<*>) {
        val entity = event.entity as? ScopeToken ?: return
        val client = clientFinder.findByName(property.name) ?: return

        clientScopeRepository.create(
            ClientScope(
                clientId = client.id!!,
                scopeTokenId = entity.id!!
            )
        )
    }
}
