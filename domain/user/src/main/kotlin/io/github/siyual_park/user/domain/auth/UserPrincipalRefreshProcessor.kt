package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.PrincipalMapping
import io.github.siyual_park.auth.domain.principal_refresher.PrincipalRefreshProcessor
import io.github.siyual_park.persistence.loadOrFail
import io.github.siyual_park.user.domain.UserStorage
import kotlinx.coroutines.flow.toSet
import org.springframework.stereotype.Component

@Component
@PrincipalMapping(UserPrincipal::class)
class UserPrincipalRefreshProcessor(
    private val userStorage: UserStorage,
) : PrincipalRefreshProcessor<UserPrincipal> {
    override suspend fun refresh(principal: UserPrincipal): UserPrincipal {
        val user = userStorage.loadOrFail(principal.userId)
        val userScope = user.getResolvedScope().toSet()

        return UserPrincipal(
            id = principal.id,
            clientId = principal.clientId,
            scope = principal.scope.filter { userScope.contains(it) }.toSet()
        )
    }
}
