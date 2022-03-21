package io.github.siyual_park.user.repository

import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.repository.r2dbc.CachedR2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.github.siyual_park.user.entity.UserScopeData
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository

@Repository
class UserScopeRepository(
    entityOperations: R2dbcEntityOperations
) : R2DBCRepository<UserScopeData, Long> by CachedR2DBCRepository.of(
    entityOperations,
    UserScopeData::class
) {
    fun findAllByUserId(userId: Long): Flow<UserScopeData> {
        return findAll(where(UserScopeData::userId).`is`(userId))
    }

    fun findAllByScopeTokenId(scopeTokenId: Long): Flow<UserScopeData> {
        return findAll(where(UserScopeData::scopeTokenId).`is`(scopeTokenId))
    }

    suspend fun deleteAllByUserId(userId: Long) {
        deleteAll(where(UserScopeData::userId).`is`(userId))
    }
}
