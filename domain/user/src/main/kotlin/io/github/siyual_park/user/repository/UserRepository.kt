package io.github.siyual_park.user.repository

import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.github.siyual_park.user.entity.User
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository

@Repository
class UserRepository(
    entityOperations: R2dbcEntityOperations
) : R2DBCRepository<User, Long>(
    entityOperations,
    User::class,
) {
    suspend fun findByNameOrFail(name: String): User {
        return findByName(name) ?: throw EmptyResultDataAccessException(1)
    }

    suspend fun findByName(name: String): User? {
        return findOne(where(User::name).`is`(name))
    }

    suspend fun existsByName(name: String): Boolean {
        return exists(where(User::name).`is`(name))
    }
}
