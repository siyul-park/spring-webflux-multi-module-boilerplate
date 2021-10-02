package io.github.siyual_park.user.repository

import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.github.siyual_park.user.entity.User
import io.github.siyual_park.user.entity.UserCredential
import io.github.siyual_park.user.entity.UserScope
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository

@Repository
class UserCredentialRepository(
    entityTemplate: R2dbcEntityTemplate
) : R2DBCRepository<UserCredential, Long>(
    entityTemplate,
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

    suspend fun deleteByUser(user: User) {
        user.id?.let { deleteByUserId(it) }
    }

    suspend fun deleteByUserId(userId: Long) {
        deleteAll(where(UserScope::userId).`is`(userId))
    }
}
