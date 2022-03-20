package io.github.siyual_park.auth.repository

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.repository.r2dbc.CachedR2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class ScopeTokenRepository(
    entityOperations: R2dbcEntityOperations
) : R2DBCRepository<ScopeToken, Long> by CachedR2DBCRepository.of(
    entityOperations,
    ScopeToken::class,
    {
        CacheBuilder.newBuilder()
            .softValues()
            .expireAfterAccess(Duration.ofMinutes(10))
            .expireAfterWrite(Duration.ofMinutes(30))
            .maximumSize(1_000)
    }
) {
    suspend fun findByNameOrFail(name: String): ScopeToken {
        return findByName(name) ?: throw EmptyResultDataAccessException(1)
    }

    suspend fun findByName(name: String): ScopeToken? {
        return findOne(where(ScopeToken::name).`is`(name))
    }

    suspend fun existsByName(name: String): Boolean {
        return exists(where(ScopeToken::name).`is`(name))
    }

    fun findAllByName(names: Iterable<String>): Flow<ScopeToken> {
        return findAll(where(ScopeToken::name).`in`(names.toList()))
    }
}
