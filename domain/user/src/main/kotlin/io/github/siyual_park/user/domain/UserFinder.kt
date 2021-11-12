package io.github.siyual_park.user.domain

import io.github.siyual_park.reader.finder.Finder
import io.github.siyual_park.reader.finder.FinderAdapter
import io.github.siyual_park.user.entity.User
import io.github.siyual_park.user.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class UserFinder(
    private val userRepository: UserRepository
) : Finder<User, Long> by FinderAdapter(userRepository) {
    suspend fun findByNameOrFail(name: String): User {
        return userRepository.findByNameOrFail(name)
    }
}
