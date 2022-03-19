package io.github.siyual_park.user.domain

import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.repository.r2dbc.findOneOrFail
import io.github.siyual_park.reader.finder.FilteredFinder
import io.github.siyual_park.user.entity.User
import io.github.siyual_park.user.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class UserFinder(
    private val userRepository: UserRepository
) : FilteredFinder<User, Long>(userRepository, where(User::deletedAt).isNull) {
    suspend fun findByNameOrFail(name: String): User {
        return userRepository.findOneOrFail(applyFilter(where(User::name).`is`(name)))
    }
}
