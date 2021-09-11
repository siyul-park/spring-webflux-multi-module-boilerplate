package io.github.siyual_park.auth.domain

import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.auth.entity.User
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.auth.repository.UserScopeRepository
import io.github.siyual_park.data.repository.findByIdOrFail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Component

@Component
class ScopeFinder(
    private val scopeTokenRepository: ScopeTokenRepository,
    private val userScopeRepository: UserScopeRepository
) {
    fun findByUser(user: User): Flow<ScopeToken> {
        return findByUserId(user.id!!)
    }

    fun findByUserId(userId: Long): Flow<ScopeToken> {
        return userScopeRepository.findAllByUserId(userId)
            .map { scopeTokenRepository.findByIdOrFail(it.scopeTokenId) }
    }

    fun findByName(names: Iterable<String>): Flow<ScopeToken> {
        return scopeTokenRepository.findAllByName(names)
    }

    fun findById(ids: Iterable<Long>): Flow<ScopeToken> {
        return scopeTokenRepository.findAllById(ids)
    }
}
