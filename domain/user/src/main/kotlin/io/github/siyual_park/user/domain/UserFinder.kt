package io.github.siyual_park.user.domain

import io.github.siyual_park.data.repository.findByIdOrFail
import io.github.siyual_park.user.entity.User
import io.github.siyual_park.user.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Component

@Component
class UserFinder(
    private val userRepository: UserRepository
) {
    fun findAllById(ids: Iterable<Long>): Flow<User> {
        return userRepository.findAllById(ids)
    }

    suspend fun findByIdOrFail(id: Long): User {
        return userRepository.findByIdOrFail(id)
    }

    suspend fun findByNameOrFail(name: String): User {
        return userRepository.findByNameOrFail(name)
    }
}
