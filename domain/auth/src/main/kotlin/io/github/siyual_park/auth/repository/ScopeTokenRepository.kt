package io.github.siyual_park.auth.repository

import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository

@Repository
class ScopeTokenRepository(
    entityTemplate: R2dbcEntityTemplate,
) : R2DBCRepository<ScopeToken, Long>(
    entityTemplate,
    ScopeToken::class,
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

    fun findAllByDefault(default: Boolean): Flow<ScopeToken> {
        return findAll(where(ScopeToken::default).`is`(default))
    }

    fun findAllByName(names: Iterable<String>): Flow<ScopeToken> {
        return findAll(where(ScopeToken::name).`in`(names.toList()))
    }
}
