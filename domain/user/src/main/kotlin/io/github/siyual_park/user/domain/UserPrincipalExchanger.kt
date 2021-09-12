package io.github.siyual_park.user.domain

import io.github.siyual_park.user.entity.User
import kotlinx.coroutines.flow.toSet
import org.springframework.stereotype.Component

@Component
class UserPrincipalExchanger(
    private val userFinder: UserFinder,
    private val userScopeFinder: UserScopeFinder
) {
    suspend fun exchange(user: User): UserPrincipal {
        val scope = userScopeFinder.findAllByUser(user).toSet()
        return UserPrincipal(
            id = user.id.toString(),
            scope = scope.toSet()
        )
    }

    suspend fun exchange(userAuthentication: UserPrincipal): User {
        return userFinder.findByIdOrFail(userAuthentication.id.toLong())
    }
}
