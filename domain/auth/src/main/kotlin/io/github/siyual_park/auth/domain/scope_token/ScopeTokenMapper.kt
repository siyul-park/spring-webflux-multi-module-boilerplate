package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.auth.entity.ScopeTokenData
import io.github.siyual_park.auth.repository.ScopeRelationRepository
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator

@Component
class ScopeTokenMapper(
    private val scopeTokenRepository: ScopeTokenRepository,
    private val scopeRelationRepository: ScopeRelationRepository,
    private val operator: TransactionalOperator,
    private val eventPublisher: EventPublisher,
) : Mapper<ScopeTokenData, ScopeToken> {
    override val sourceType = object : TypeReference<ScopeTokenData>() {}
    override val targetType = object : TypeReference<ScopeToken>() {}

    override suspend fun map(source: ScopeTokenData): ScopeToken {
        return ScopeToken(
            source,
            scopeTokenRepository,
            scopeRelationRepository,
            operator,
            eventPublisher
        )
    }
}
