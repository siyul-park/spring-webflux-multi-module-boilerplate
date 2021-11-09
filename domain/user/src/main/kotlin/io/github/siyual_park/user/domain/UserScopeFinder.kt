package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.search.finder.R2dbcFinder
import io.github.siyual_park.user.entity.User
import io.github.siyual_park.user.entity.UserScope
import io.github.siyual_park.user.repository.UserScopeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class UserScopeFinder(
    private val userScopeRepository: UserScopeRepository,
    private val scopeTokenRepository: ScopeTokenRepository
) : R2dbcFinder<UserScope, Long>(userScopeRepository) {
    fun findAllByUser(user: User): Flow<ScopeToken> {
        return user.id?.let { findAllByUserId(it) } ?: emptyFlow()
    }

    fun findAllByUserId(userId: Long): Flow<ScopeToken> {
        return flow {
            val userScopes = userScopeRepository.findAllByUserId(userId).toList()
            emitAll(scopeTokenRepository.findAllById(userScopes.map { it.scopeTokenId }))
        }
    }
}
