package io.github.siyual_park.user.event.consumer

import io.github.siyual_park.auth.repository.TokenEntityRepository
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.data.event.AfterDeleteEvent
import io.github.siyual_park.data.transaction.SuspendTransactionSynchronization
import io.github.siyual_park.event.EventConsumer
import io.github.siyual_park.event.Subscribe
import io.github.siyual_park.user.entity.UserEntity
import org.springframework.stereotype.Component

@Component
@Subscribe(filterBy = AfterDeleteEvent::class)
class CascadeDeleteToken(
    private val tokenEntityRepository: TokenEntityRepository
) : EventConsumer<AfterDeleteEvent<*>> {
    override suspend fun consume(event: AfterDeleteEvent<*>) {
        val entity = event.entity as? UserEntity ?: return

        SuspendTransactionSynchronization.beforeCommit {
            tokenEntityRepository.deleteAll(where("claims.uid").`is`(entity.id.toString()))
        }
    }
}
