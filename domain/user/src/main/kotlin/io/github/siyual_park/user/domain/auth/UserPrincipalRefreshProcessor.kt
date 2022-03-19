package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.PrincipalMapping
import io.github.siyual_park.auth.domain.principal_refresher.PrincipalRefreshProcessor
import io.github.siyual_park.user.domain.UserScopeFinder
import kotlinx.coroutines.flow.toSet
import org.springframework.stereotype.Component

@Component
@PrincipalMapping(UserPrincipal::class)
class UserPrincipalRefreshProcessor(
    private val userScopeFinder: UserScopeFinder,
) : PrincipalRefreshProcessor<UserPrincipal> {
    override suspend fun refresh(principal: UserPrincipal): UserPrincipal {
        val exitedScope = userScopeFinder.findAllWithResolvedByUserId(principal.userId).toSet()
        return UserPrincipal(
            id = principal.id,
            clientId = principal.clientId,
            scope = principal.scope.filter { exitedScope.contains(it) }.toSet()
        )
    }
}
