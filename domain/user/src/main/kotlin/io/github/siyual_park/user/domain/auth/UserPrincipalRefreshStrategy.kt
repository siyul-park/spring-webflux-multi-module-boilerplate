package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.PrincipalMapping
import io.github.siyual_park.auth.domain.principal_refresher.PrincipalRefreshStrategy
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.data.cache.SuspendLazy
import io.github.siyual_park.persistence.loadOrFail
import io.github.siyual_park.user.domain.UserStorage
import kotlinx.coroutines.flow.toSet
import org.springframework.stereotype.Component

@Component
@PrincipalMapping(UserPrincipal::class)
class UserPrincipalRefreshStrategy(
    private val userStorage: UserStorage,
    private val scopeTokenStorage: ScopeTokenStorage
) : PrincipalRefreshStrategy<UserPrincipal> {
    private val accessTokenScope = SuspendLazy {
        scopeTokenStorage.loadOrFail("access-token:create")
    }
    private val refreshTokenScope = SuspendLazy {
        scopeTokenStorage.loadOrFail("refresh-token:create")
    }

    override suspend fun refresh(principal: UserPrincipal): UserPrincipal {
        val user = userStorage.loadOrFail(principal.userId)
        val userScope = user.getScope().toSet()

        return UserPrincipal(
            id = principal.id,
            clientId = principal.clientId,
            scope = mutableSetOf<ScopeToken>().apply {
                addAll(userScope)
                addAll(principal.scope)
            }
                .filter { it.id != refreshTokenScope.get().id && it.id != accessTokenScope.get().id }
                .toSet()
        )
    }
}
