package io.github.siyual_park.user.domain

import io.github.siyual_park.search.finder.R2dbcFinder
import io.github.siyual_park.user.entity.User
import io.github.siyual_park.user.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class UserFinder(
    private val userRepository: UserRepository
) : R2dbcFinder<User, Long>(userRepository) {
    suspend fun findByNameOrFail(name: String): User {
        return userRepository.findByNameOrFail(name)
    }
}
