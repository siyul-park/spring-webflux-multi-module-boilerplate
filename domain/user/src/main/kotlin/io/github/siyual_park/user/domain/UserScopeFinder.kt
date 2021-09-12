package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.user.entity.User
import io.github.siyual_park.user.repository.UserScopeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Component

@Component
class UserScopeFinder(
    private val userScopeRepository: UserScopeRepository,
    private val scopeTokenRepository: ScopeTokenRepository
) {
    fun findAllByUser(user: User): Flow<ScopeToken> {
        return user.id?.let { findAllByUserId(it) } ?: emptyFlow()
    }

    fun findAllByUserId(userId: Long): Flow<ScopeToken> {
        return userScopeRepository.findAllByUserId(userId)
            .map { scopeTokenRepository.findById(it.scopeTokenId) }
            .filterNotNull()
    }
}
