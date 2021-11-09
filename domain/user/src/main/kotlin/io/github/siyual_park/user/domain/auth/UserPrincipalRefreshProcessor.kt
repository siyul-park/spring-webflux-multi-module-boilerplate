package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.principal_refresher.PrincipalRefreshProcessor
import io.github.siyual_park.user.domain.UserScopeFinder
import kotlinx.coroutines.flow.toSet
import org.springframework.stereotype.Component

@Suppress("UNCHECKED_CAST")
@Component
class UserPrincipalRefreshProcessor(
    private val userScopeFinder: UserScopeFinder,
) : PrincipalRefreshProcessor<UserPrincipal> {
    override val principalClazz = UserPrincipal::class

    override suspend fun refresh(principal: UserPrincipal): UserPrincipal {
        val exitedScope = userScopeFinder.findAllByUserId(principal.userId).toSet()
        return UserPrincipal(
            id = principal.id,
            scope = principal.scope.filter { exitedScope.contains(it) }.toSet()
        )
    }
}
