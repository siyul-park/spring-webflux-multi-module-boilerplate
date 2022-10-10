package io.github.siyual_park.user.event.consumer

import io.github.siyual_park.auth.repository.TokenDataRepository
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.data.event.AfterDeleteEvent
import io.github.siyual_park.data.transaction.SuspendTransactionSynchronization
import io.github.siyual_park.event.EventConsumer
import io.github.siyual_park.event.Subscribe
import io.github.siyual_park.user.entity.UserData
import org.springframework.stereotype.Component

@Component
@Subscribe(filterBy = AfterDeleteEvent::class)
class CascadeDeleteToken(
    private val tokenDataRepository: TokenDataRepository
) : EventConsumer<AfterDeleteEvent<*>> {
    override suspend fun consume(event: AfterDeleteEvent<*>) {
        val entity = event.entity as? UserData ?: return

        SuspendTransactionSynchronization.beforeCommit {
            tokenDataRepository.deleteAll(where("claims.uid").`is`(entity.id.toString()))
        }
    }
}
