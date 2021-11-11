package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFinder
import io.github.siyual_park.auth.entity.ScopeRelation
import io.github.siyual_park.client.entity.Client
import io.github.siyual_park.client.entity.ClientScope
import io.github.siyual_park.client.repository.ClientRepository
import io.github.siyual_park.client.repository.ClientScopeRepository
import io.github.siyual_park.data.event.AfterSaveEvent
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.event.EventConsumer
import io.github.siyual_park.event.Subscribe
import io.github.siyual_park.search.finder.findByIdOrFail
import io.github.siyual_park.search.pagination.OffsetPaginatorAdapter
import io.github.siyual_park.search.pagination.forEach
import org.springframework.stereotype.Component

@Component
@Subscribe(filterBy = AfterSaveEvent::class)
class SyncClientScope(
    clientRepository: ClientRepository,
    private val clientScopeRepository: ClientScopeRepository,
    private val scopeTokenFinder: ScopeTokenFinder,
) : EventConsumer<AfterSaveEvent<*>> {
    private val clientPaginator = OffsetPaginatorAdapter(
        clientRepository,
        criteria = where(Client::deletedAt).isNull
    )

    override suspend fun consume(event: AfterSaveEvent<*>) {
        val entity = event.entity as? ScopeRelation ?: return
        val parent = scopeTokenFinder.findByIdOrFail(entity.parentId)
        if (parent.name != "client") {
            return
        }

        clientPaginator.forEach(100) { users ->
            users.map {
                ClientScope(
                    clientId = it.id!!,
                    scopeTokenId = entity.childId
                )
            }.let { clientScopeRepository.createAll(it) }
        }
    }
}
