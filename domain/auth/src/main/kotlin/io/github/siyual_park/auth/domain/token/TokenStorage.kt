package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.entity.TokenData
import io.github.siyual_park.auth.repository.TokenRepository
import io.github.siyual_park.persistence.MongoStorage
import io.github.siyual_park.persistence.SimpleMongoStorage
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.CriteriaDefinition
import org.springframework.data.mongodb.core.query.and
import org.springframework.data.mongodb.core.query.where
import org.springframework.stereotype.Component

@Component
class TokenStorage(
    tokenRepository: TokenRepository,
    tokenMapper: TokenMapper
) : MongoStorage<Token, ULID> {
    private val delegator = SimpleMongoStorage(tokenRepository) { tokenMapper.map(it) }

    fun load(type: String, claims: Map<String, Any>, limit: Int?, offset: Long?, sort: Sort?): Flow<Token> {
        var query = where(TokenData::type).`is`(type)
        claims.forEach { (key, value) ->
            query = query.and("claims.$key").`is`(value)
        }

        return load(query, limit, offset, sort)
    }

    suspend fun loadOrFail(signature: String): Token {
        return load(signature) ?: throw EmptyResultDataAccessException(1)
    }

    suspend fun load(signature: String): Token? {
        return load(where(TokenData::signature).`is`(signature))
    }

    override suspend fun load(criteria: CriteriaDefinition): Token? {
        return delegator.load(criteria)?.let { if (it.isActivated()) it else null }
    }

    override fun load(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?): Flow<Token> {
        return delegator.load(criteria, limit, offset, sort).filter { it.isActivated() }
    }

    override suspend fun count(criteria: CriteriaDefinition?): Long {
        return delegator.count(criteria)
    }

    override suspend fun load(id: ULID): Token? {
        return delegator.load(id)?.let { if (it.isActivated()) it else null }
    }

    override fun load(ids: Iterable<ULID>): Flow<Token> {
        return delegator.load(ids).filter { it.isActivated() }
    }

    override suspend fun count(): Long {
        return delegator.count()
    }
}
