package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.PrincipalMapping
import io.github.siyual_park.auth.domain.principal_refresher.PrincipalRefreshStrategy
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.persistence.loadOrFail
import io.github.siyual_park.user.domain.UserStorage
import kotlinx.coroutines.flow.toSet
import org.springframework.stereotype.Component

@Component
@PrincipalMapping(UserPrincipal::class)
class UserPrincipalRefreshStrategy(
    private val userStorage: UserStorage,
) : PrincipalRefreshStrategy<UserPrincipal> {
    override suspend fun refresh(principal: UserPrincipal): UserPrincipal {
        val user = userStorage.loadOrFail(principal.userId)
        val userScope = user.getScope().toSet()

        return UserPrincipal(
            id = principal.id,
            userId = principal.userId,
            clientId = principal.clientId,
            scope = userScope
        )
    }
}
