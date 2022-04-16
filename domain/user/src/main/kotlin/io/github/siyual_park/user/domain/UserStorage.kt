package io.github.siyual_park.user.domain

import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.persistence.R2DBCStorage
import io.github.siyual_park.persistence.SimpleR2DBCStorage
import io.github.siyual_park.ulid.ULID
import io.github.siyual_park.user.entity.UserData
import io.github.siyual_park.user.repository.UserRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Component

@Component
class UserStorage(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper
) : R2DBCStorage<User, ULID> by SimpleR2DBCStorage(
    userRepository,
    { userMapper.map(it) }
) {
    suspend fun load(name: String): User? {
        return load(where(UserData::name).`is`(name))
    }
}

suspend fun UserStorage.loadOrFail(name: String): User {
    return load(name) ?: throw EmptyResultDataAccessException(1)
}
