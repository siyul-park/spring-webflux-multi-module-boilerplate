package io.github.siyual_park.auth.repository

import io.github.siyual_park.auth.entity.User
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.r2dbc.spi.ConnectionFactory
import org.springframework.stereotype.Repository

@Repository
class UserRepository(
    connectionFactory: ConnectionFactory
) : R2DBCRepository<User, Long>(
    connectionFactory,
    User::class,
) {
    suspend fun findByName(name: String): User? {
        return findOne(where(User::name).`is`(name))
    }

    suspend fun existsByName(name: String): Boolean {
        return exists(where(User::name).`is`(name))
    }
}
