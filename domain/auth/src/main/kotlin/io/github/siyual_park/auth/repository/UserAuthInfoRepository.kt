package io.github.siyual_park.auth.repository

import io.github.siyual_park.auth.entity.User
import io.github.siyual_park.auth.entity.UserAuthInfo
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.r2dbc.spi.ConnectionFactory
import org.springframework.stereotype.Repository

@Repository
class UserAuthInfoRepository(
    connectionFactory: ConnectionFactory
) : R2DBCRepository<UserAuthInfo, Long>(
    connectionFactory,
    UserAuthInfo::class,
) {
    suspend fun findByUser(user: User): UserAuthInfo? {
        return user.id?.let { findByUserId(it) }
    }

    suspend fun findByUserId(userId: Long): UserAuthInfo? {
        return findOne(where(UserAuthInfo::userId).`is`(userId))
    }

    suspend fun existsByUser(user: User): Boolean {
        return user.id?.let { existsByUserId(it) } ?: false
    }

    suspend fun existsByUserId(userId: Long): Boolean {
        return exists(where(UserAuthInfo::userId).`is`(userId))
    }
}
