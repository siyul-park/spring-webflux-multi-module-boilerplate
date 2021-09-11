package io.github.siyual_park.auth.domain.authenticator

import io.github.siyual_park.auth.domain.ScopeFinder
import io.github.siyual_park.auth.entity.User
import io.github.siyual_park.auth.repository.UserRepository
import io.github.siyual_park.data.repository.findByIdOrFail
import kotlinx.coroutines.flow.toSet
import org.springframework.stereotype.Component

@Component
class UserAuthenticationExchanger(
    private val userRepository: UserRepository,
    private val scopeFinder: ScopeFinder,
) {
    suspend fun exchange(user: User): UserAuthentication {
        val scope = scopeFinder.find(user)
        return UserAuthentication(
            id = user.id!!,
            scope = scope.toSet()
        )
    }

    suspend fun exchange(userAuthentication: UserAuthentication): User {
        return userRepository.findByIdOrFail(userAuthentication.id)
    }
}
