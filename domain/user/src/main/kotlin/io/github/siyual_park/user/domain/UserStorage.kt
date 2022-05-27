package io.github.siyual_park.user.domain

import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.persistence.QueryStorage
import io.github.siyual_park.persistence.SimpleQueryStorage
import io.github.siyual_park.ulid.ULID
import io.github.siyual_park.user.entity.UserData
import io.github.siyual_park.user.repository.UserRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Component

@Component
class UserStorage(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper,
    private val usersMapper: UsersMapper,
) : QueryStorage<User, ULID> by SimpleQueryStorage(
    userRepository,
    { userMapper.map(it) },
    { usersMapper.map(it) }
) {
    suspend fun load(name: String): User? {
        return load(where(UserData::name).`is`(name))
    }
}

suspend fun UserStorage.loadOrFail(name: String): User {
    return load(name) ?: throw EmptyResultDataAccessException(1)
}
