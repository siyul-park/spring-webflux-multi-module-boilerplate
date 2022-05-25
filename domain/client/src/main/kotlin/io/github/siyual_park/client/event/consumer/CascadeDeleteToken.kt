package io.github.siyual_park.client.event.consumer

import io.github.siyual_park.auth.repository.TokenRepository
import io.github.siyual_park.client.entity.ClientData
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.data.event.AfterDeleteEvent
import io.github.siyual_park.data.transaction.doAfterCommit
import io.github.siyual_park.event.EventConsumer
import io.github.siyual_park.event.Subscribe
import org.springframework.stereotype.Component

@Component
@Subscribe(filterBy = AfterDeleteEvent::class)
class CascadeDeleteToken(
    private val tokenRepository: TokenRepository
) : EventConsumer<AfterDeleteEvent<*>> {
    override suspend fun consume(event: AfterDeleteEvent<*>) {
        val entity = event.entity as? ClientData ?: return

        doAfterCommit {
            tokenRepository.deleteAll(where("claims.cid").`is`(entity.id.toString()))
        }
    }
}
