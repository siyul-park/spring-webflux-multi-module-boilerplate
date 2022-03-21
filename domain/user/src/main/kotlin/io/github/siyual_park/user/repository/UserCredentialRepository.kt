package io.github.siyual_park.user.repository

import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.repository.r2dbc.CachedR2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.github.siyual_park.user.entity.UserCredentialData
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository

@Repository
class UserCredentialRepository(
    entityOperations: R2dbcEntityOperations
) : R2DBCRepository<UserCredentialData, Long> by CachedR2DBCRepository.of(
    entityOperations,
    UserCredentialData::class
) {
    suspend fun findByUserIdOrFail(userId: Long): UserCredentialData {
        return findByUserId(userId) ?: throw EmptyResultDataAccessException(1)
    }

    suspend fun findByUserId(userId: Long): UserCredentialData? {
        return findOne(where(UserCredentialData::userId).`is`(userId))
    }

    suspend fun existsByUserId(userId: Long): Boolean {
        return exists(where(UserCredentialData::userId).`is`(userId))
    }

    suspend fun deleteByUserId(userId: Long) {
        deleteAll(where(UserCredentialData::userId).`is`(userId))
    }
}
