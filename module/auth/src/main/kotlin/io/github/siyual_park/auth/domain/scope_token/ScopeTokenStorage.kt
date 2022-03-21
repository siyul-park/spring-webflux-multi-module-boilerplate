package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.auth.entity.ScopeTokenData
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.persistence.R2DBCStorage
import io.github.siyual_park.persistence.SimpleR2DBCStorage
import org.springframework.stereotype.Component

@Component
class ScopeTokenStorage(
    private val scopeTokenRepository: ScopeTokenRepository,
    private val scopeTokenMapper: ScopeTokenMapper
) : R2DBCStorage<ScopeTokenData, Long, ScopeToken> by SimpleR2DBCStorage(
    scopeTokenRepository,
    { scopeTokenMapper.map(it) }
)
