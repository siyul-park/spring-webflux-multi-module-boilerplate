package io.github.siyual_park.user.domain

import io.github.siyual_park.data.repository.findByIdOrFail
import io.github.siyual_park.user.entity.User
import io.github.siyual_park.user.repository.UserRepository
import kotlinx.coroutines.flow.toSet
import org.springframework.stereotype.Component

@Component
class UserPrincipalExchanger(
    private val userRepository: UserRepository,
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
        return userRepository.findByIdOrFail(userAuthentication.id.toLong())
    }
}
