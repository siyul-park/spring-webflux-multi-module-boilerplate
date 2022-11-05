package io.github.siyual_park.client.event.consumer

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenMapper
import io.github.siyual_park.auth.entity.ScopeTokenEntity
import io.github.siyual_park.client.domain.ClientStorage
import io.github.siyual_park.client.property.RootClientProperty
import io.github.siyual_park.data.event.AfterCreateEvent
import io.github.siyual_park.event.EventConsumer
import io.github.siyual_park.event.Subscribe
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component

@Component
@Subscribe(filterBy = AfterCreateEvent::class)
class SyncRootClientScope(
    private val clientStorage: ClientStorage,
    private val scopeTokenMapper: ScopeTokenMapper,
    private val property: RootClientProperty,
) : EventConsumer<AfterCreateEvent<*>> {
    override suspend fun consume(event: AfterCreateEvent<*>) {
        val entity = (event.entity as? ScopeTokenEntity)?.let { scopeTokenMapper.map(it) } ?: return
        val client = clientStorage.load(property.name) ?: return

        try {
            client.grant(entity)
        } catch (_: DataIntegrityViolationException) {
        }
    }
}
