package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.auth.entity.ScopeTokenEntity
import io.github.siyual_park.auth.repository.ScopeTokenEntityRepository
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.persistence.QueryableLoader
import io.github.siyual_park.persistence.SimpleQueryableLoader
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.flow.Flow
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

@Component
class ScopeTokenStorage(
    private val scopeTokenEntityRepository: ScopeTokenEntityRepository,
    private val scopeTokenMapper: ScopeTokenMapper
) : QueryableLoader<ScopeToken, ULID> by SimpleQueryableLoader(
    scopeTokenEntityRepository,
    { scopeTokenMapper.map(it) }
) {
    suspend fun upsert(name: String): ScopeToken {
        return upsert(CreateScopeTokenPayload(name = name))
    }

    suspend fun upsert(payload: CreateScopeTokenPayload): ScopeToken {
        val exited = load(payload.name)
        return if (exited != null) {
            exited.description = payload.description
            exited.sync()
            exited
        } else {
            save(payload)
        }
    }

    suspend fun save(payload: CreateScopeTokenPayload): ScopeToken {
        return scopeTokenEntityRepository.create(
            ScopeTokenEntity(
                name = payload.name,
                description = payload.description,
                system = payload.system
            )
        )
            .let { scopeTokenMapper.map(it) }
    }

    suspend fun load(name: String): ScopeToken? {
        return load(where(ScopeTokenEntity::name).`is`(name))
    }

    fun load(
        name: List<String>,
        limit: Int? = null,
        offset: Long? = null,
        sort: Sort? = null
    ): Flow<ScopeToken> {
        return load(
            where(ScopeTokenEntity::name).`in`(name),
            limit,
            offset,
            sort
        )
    }
}

suspend fun ScopeTokenStorage.loadOrFail(name: String): ScopeToken {
    return load(name) ?: throw EmptyResultDataAccessException(1)
}
