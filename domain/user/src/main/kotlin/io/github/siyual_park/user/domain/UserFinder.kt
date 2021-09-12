package io.github.siyual_park.user.domain

import io.github.siyual_park.data.repository.findByIdOrFail
import io.github.siyual_park.user.entity.User
import io.github.siyual_park.user.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class UserFinder(
    private val userRepository: UserRepository
) {
    suspend fun findByIdOrFail(id: Long): User {
        return userRepository.findByIdOrFail(id)
    }

    suspend fun findByNameOrFail(name: String): User {
        return userRepository.findByNameOrFail(name)
    }
}
