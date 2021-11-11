package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.client.entity.ClientEntity
import io.github.siyual_park.search.finder.findByIdOrFail
import io.github.siyual_park.user.domain.UserFinder
import io.github.siyual_park.user.domain.UserScopeFinder
import io.github.siyual_park.user.entity.User
import io.github.siyual_park.user.entity.UserEntity
import kotlinx.coroutines.flow.toSet
import org.springframework.stereotype.Component

@Component
class UserPrincipalExchanger(
    private val userFinder: UserFinder,
    private val userScopeFinder: UserScopeFinder
) {
    suspend fun exchange(user: UserEntity, client: ClientEntity? = null): UserPrincipal {
        val scope = userScopeFinder.findAllByUserId(user.userId!!).toSet()
        return UserPrincipal(
            id = user.userId.toString(),
            clientId = client?.clientId,
            scope = scope.toSet()
        )
    }

    suspend fun exchange(userPrincipal: UserPrincipal): User {
        return userFinder.findByIdOrFail(userPrincipal.userId)
    }
}
