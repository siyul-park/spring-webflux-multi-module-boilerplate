package io.github.siyual_park.user.repository

import io.github.siyual_park.data.repository.r2dbc.CachedR2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.where
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.ulid.ULID
import io.github.siyual_park.user.entity.UserContactData
import kotlinx.coroutines.flow.Flow
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository

@Repository
class UserContactRepository(
    entityOperations: R2dbcEntityOperations,
    eventPublisher: EventPublisher? = null
) : R2DBCRepository<UserContactData, Long> by CachedR2DBCRepository.of(
    entityOperations,
    UserContactData::class,
    eventPublisher = eventPublisher
) {
    suspend fun findAllByUserId(userIds: Iterable<ULID>): Flow<UserContactData> {
        return findAll(where(UserContactData::userId).`in`(userIds.toList()))
    }

    suspend fun findByUserIdOrFail(userId: ULID): UserContactData {
        return findByUserId(userId) ?: throw EmptyResultDataAccessException(1)
    }

    suspend fun findByUserId(userId: ULID): UserContactData? {
        return findOne(where(UserContactData::userId).`is`(userId))
    }

    suspend fun existsByUserId(userId: ULID): Boolean {
        return exists(where(UserContactData::userId).`is`(userId))
    }

    suspend fun deleteByUserId(userId: ULID) {
        deleteAll(where(UserContactData::userId).`is`(userId))
    }
}
