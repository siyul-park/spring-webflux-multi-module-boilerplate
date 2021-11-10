package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFinder
import io.github.siyual_park.auth.entity.ScopeRelation
import io.github.siyual_park.data.callback.AfterSaveCallback
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.search.finder.findByIdOrFail
import io.github.siyual_park.search.pagination.OffsetPaginatorAdapter
import io.github.siyual_park.search.pagination.forEach
import io.github.siyual_park.user.entity.User
import io.github.siyual_park.user.entity.UserScope
import io.github.siyual_park.user.repository.UserRepository
import io.github.siyual_park.user.repository.UserScopeRepository
import org.springframework.stereotype.Component

@Component
class SyncUserScope(
    userRepository: UserRepository,
    private val userScopeRepository: UserScopeRepository,
    private val scopeTokenFinder: ScopeTokenFinder,
) : AfterSaveCallback<ScopeRelation> {
    override val clazz = ScopeRelation::class

    private val userPaginator = OffsetPaginatorAdapter(
        userRepository,
        criteria = where(User::deletedAt).isNull
    )

    override suspend fun onAfterSave(entity: ScopeRelation) {
        val parent = scopeTokenFinder.findByIdOrFail(entity.parentId)
        if (parent.name != "user") {
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
