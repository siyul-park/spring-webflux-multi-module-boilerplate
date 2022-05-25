package io.github.siyual_park.user.event.consumer

import io.github.siyual_park.auth.repository.TokenRepository
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.data.event.AfterDeleteEvent
import io.github.siyual_park.data.transaction.doAfterCommit
import io.github.siyual_park.event.EventConsumer
import io.github.siyual_park.event.Subscribe
import io.github.siyual_park.user.entity.UserData
import org.springframework.stereotype.Component

@Component
@Subscribe(filterBy = AfterDeleteEvent::class)
class CascadeDeleteToken(
    private val tokenRepository: TokenRepository
) : EventConsumer<AfterDeleteEvent<*>> {
    override suspend fun consume(event: AfterDeleteEvent<*>) {
        val entity = event.entity as? UserData ?: return

        doAfterCommit {
            tokenRepository.deleteAll(where("claims.uid").`is`(entity.id.toString()))
        }
    }
}
