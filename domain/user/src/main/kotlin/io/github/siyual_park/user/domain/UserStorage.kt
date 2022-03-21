package io.github.siyual_park.user.domain

import io.github.siyual_park.persistence.R2DBCStorage
import io.github.siyual_park.persistence.SimpleR2DBCStorage
import io.github.siyual_park.user.entity.UserData
import io.github.siyual_park.user.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class UserStorage(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper
) : R2DBCStorage<UserData, Long, User> by SimpleR2DBCStorage(
    userRepository,
    { userMapper.map(it) }
)
