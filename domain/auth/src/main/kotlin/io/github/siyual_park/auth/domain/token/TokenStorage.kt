package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.entity.TokenData
import io.github.siyual_park.auth.repository.RawTokenRepository
import io.github.siyual_park.auth.repository.TokenRepository
import io.github.siyual_park.data.expansion.columnName
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.persistence.R2DBCStorage
import io.github.siyual_park.persistence.SimpleR2DBCStorage
import io.github.siyual_park.util.tickerFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.query.CriteriaDefinition
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

@Component
class TokenStorage(
    tokenRepository: TokenRepository,
    rawTokenRepository: RawTokenRepository,
    tokenMapper: TokenMapper
) : R2DBCStorage<Token, Long> {
    private val logger = LoggerFactory.getLogger(TokenStorage::class.java)

    private val delegator = SimpleR2DBCStorage(tokenRepository) { tokenMapper.map(it) }

    private val expireJob = tickerFlow(Duration.ofMinutes(1))
        .onEach {
            try {
                delay(Random.nextLong(Duration.ofSeconds(30).toMillis()))

                rawTokenRepository.deleteAll(
                    where(TokenData::expiredAt).lessThanOrEquals(Instant.now()),
                    sort = Sort.by(Sort.Order.asc(columnName(TokenData::expiredAt))),
                    limit = 200
                )
            } catch (e: Exception) {
                logger.error(e.message)
            }
        }.launchIn(CoroutineScope(Dispatchers.IO))

    suspend fun loadOrFail(signature: String): Token {
        return load(signature) ?: throw EmptyResultDataAccessException(1)
    }

    suspend fun load(signature: String): Token? {
        return load(where(TokenData::signature).`is`(signature))
    }

    override suspend fun load(criteria: CriteriaDefinition): Token? {
        return delegator.load(criteria)
    }

    override fun load(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?): Flow<Token> {
        return delegator.load(criteria, limit, offset, sort)
    }

    override suspend fun count(criteria: CriteriaDefinition?): Long {
        return delegator.count(criteria)
    }

    override suspend fun load(id: Long): Token? {
        return delegator.load(id)
    }

    override fun load(ids: Iterable<Long>): Flow<Token> {
        return delegator.load(ids)
    }

    override suspend fun count(): Long {
        return delegator.count()
    }

    fun clear() {
        expireJob.cancel()
    }
}
