package io.github.siyual_park.auth.repository

import io.github.siyual_park.auth.entity.User
import io.github.siyual_park.auth.entity.UserCredential
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.r2dbc.spi.ConnectionFactory
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Repository

@Repository
class UserCredentialRepository(
    connectionFactory: ConnectionFactory
) : R2DBCRepository<UserCredential, Long>(
    connectionFactory,
    UserCredential::class,
) {
    suspend fun findByUserOrFail(user: User): UserCredential {
        return findByUser(user) ?: throw EmptyResultDataAccessException(1)
    }

    suspend fun findByUser(user: User): UserCredential? {
        return user.id?.let { findByUserId(it) }
    }

    suspend fun findByUserIdOrFail(userId: Long): UserCredential {
        return findByUserId(userId) ?: throw EmptyResultDataAccessException(1)
    }

    suspend fun findByUserId(userId: Long): UserCredential? {
        return findOne(where(UserCredential::userId).`is`(userId))
    }

    suspend fun existsByUser(user: User): Boolean {
        return user.id?.let { existsByUserId(it) } ?: false
    }

    suspend fun existsByUserId(userId: Long): Boolean {
        return exists(where(UserCredential::userId).`is`(userId))
    }
}
