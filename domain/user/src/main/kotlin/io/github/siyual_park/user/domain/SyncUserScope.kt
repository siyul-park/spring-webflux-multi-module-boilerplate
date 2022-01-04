package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFinder
import io.github.siyual_park.auth.entity.ScopeRelation
import io.github.siyual_park.data.event.AfterSaveEvent
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.event.EventConsumer
import io.github.siyual_park.event.Subscribe
import io.github.siyual_park.reader.finder.findByIdOrFail
import io.github.siyual_park.reader.pagination.OffsetPaginatorAdapter
import io.github.siyual_park.reader.pagination.forEach
import io.github.siyual_park.user.entity.User
import io.github.siyual_park.user.entity.UserScope
import io.github.siyual_park.user.repository.UserRepository
import io.github.siyual_park.user.repository.UserScopeRepository
import org.springframework.stereotype.Component

@Component
@Subscribe(filterBy = AfterSaveEvent::class)
class SyncUserScope(
    userRepository: UserRepository,
    private val userScopeRepository: UserScopeRepository,
    private val scopeTokenFinder: ScopeTokenFinder,
) : EventConsumer<AfterSaveEvent<*>> {
    private val userPaginator = OffsetPaginatorAdapter(
        userRepository,
        criteria = where(User::deletedAt).isNull
    )

    override suspend fun consume(event: AfterSaveEvent<*>) {
        val entity = event.entity as? ScopeRelation ?: return
        val parent = scopeTokenFinder.findByIdOrFail(entity.parentId)
        if (parent.name != "pack:user") {
            return
        }

        userPaginator.forEach(100) { users ->
            users.map {
                UserScope(
                    userId = it.id!!,
                    scopeTokenId = entity.childId
                )
            }.let { userScopeRepository.createAll(it) }
        }
    }
}
