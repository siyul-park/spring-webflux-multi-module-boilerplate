package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.entity.TokenData
import io.github.siyual_park.auth.repository.TokenRepository
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.persistence.R2DBCStorage
import io.github.siyual_park.persistence.SimpleR2DBCStorage
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.query.CriteriaDefinition
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class TokenStorage(
    tokenRepository: TokenRepository,
    tokenMapper: TokenMapper
) : R2DBCStorage<Token, ULID> {
    private val delegator = SimpleR2DBCStorage(tokenRepository) { tokenMapper.map(it) }

    override suspend fun load(criteria: CriteriaDefinition): Token? {
        return delegator.load(filter(criteria))
    }

    override fun load(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?): Flow<Token> {
        return delegator.load(filter(criteria), limit, offset, sort)
    }

    override suspend fun count(criteria: CriteriaDefinition?): Long {
        return delegator.count(filter(criteria))
    }

    override suspend fun load(id: ULID): Token? {
        return delegator.load(id)?.let { if (it.isActivated()) null else it }
    }

    override fun load(ids: Iterable<ULID>): Flow<Token> {
        return delegator.load(ids).filter { it.isActivated() }
    }

    override suspend fun count(): Long {
        return delegator.count(filter(null))
    }

    private fun filter(criteria: CriteriaDefinition?): CriteriaDefinition {
        if (criteria != null) {
            return where(TokenData::expiredAt).lessThan(Instant.now()).and(criteria)
        }

        return where(TokenData::expiredAt).lessThan(Instant.now())
    }
}
