package io.github.siyual_park.user.repository

import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.github.siyual_park.user.entity.User
import io.github.siyual_park.user.entity.UserScope
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.springframework.stereotype.Repository

@Repository
class UserScopeRepository(
    connectionFactory: ConnectionFactory
) : R2DBCRepository<UserScope, Long>(
    connectionFactory,
    UserScope::class,
) {
    fun findAllByUser(user: User): Flow<UserScope> {
        return user.id?.let { findAllByUserId(it) } ?: emptyFlow()
    }

    fun findAllByUserId(userId: Long): Flow<UserScope> {
        return findAll(where(UserScope::userId).`is`(userId))
    }

    fun findAllByScopeToken(scopeToken: ScopeToken): Flow<UserScope> {
        return scopeToken.id?.let { findAllByScopeTokenId(it) } ?: emptyFlow()
    }

    fun findAllByScopeTokenId(scopeTokenId: Long): Flow<UserScope> {
        return findAll(where(UserScope::scopeTokenId).`is`(scopeTokenId))
    }
}
