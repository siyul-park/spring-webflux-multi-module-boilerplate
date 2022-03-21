package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.client.property.RootClientProperty
import io.github.siyual_park.data.event.AfterSaveEvent
import io.github.siyual_park.event.EventConsumer
import io.github.siyual_park.event.Subscribe
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Component

@Component
@Subscribe(filterBy = AfterSaveEvent::class)
class SyncRootClientScope(
    private val clientStorage: ClientStorage,
    private val property: RootClientProperty,
) : EventConsumer<AfterSaveEvent<*>> {
    override suspend fun consume(event: AfterSaveEvent<*>) {
        val entity = event.entity as? ScopeToken ?: return
        val client = clientStorage.load(property.name) ?: return

        try {
            client.grant(entity)
        } catch (_: DuplicateKeyException) {
        }
    }
}
