package io.github.siyual_park.user.repository

import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.repository.r2dbc.CachedR2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.ulid.ULID
import io.github.siyual_park.user.entity.UserScopeData
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository

@Repository
class UserScopeRepository(
    entityOperations: R2dbcEntityOperations,
    eventPublisher: EventPublisher? = null
) : R2DBCRepository<UserScopeData, Long> by CachedR2DBCRepository.of(
    entityOperations,
    UserScopeData::class,
    eventPublisher = eventPublisher
) {
    fun findAllByUserId(userId: ULID): Flow<UserScopeData> {
        return findAll(where(UserScopeData::userId).`is`(userId))
    }

    fun findAllByScopeTokenId(scopeTokenId: ULID): Flow<UserScopeData> {
        return findAll(where(UserScopeData::scopeTokenId).`is`(scopeTokenId))
    }

    suspend fun deleteAllByUserId(userId: ULID) {
        deleteAll(where(UserScopeData::userId).`is`(userId))
    }

    suspend fun deleteAllByScopeTokenId(scopeTokenId: ULID) {
        deleteAll(where(UserScopeData::scopeTokenId).`is`(scopeTokenId))
    }
}
