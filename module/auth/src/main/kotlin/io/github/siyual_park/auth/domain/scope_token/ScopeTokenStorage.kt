package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.persistence.R2DBCStorage
import io.github.siyual_park.persistence.SimpleR2DBCStorage
import org.springframework.stereotype.Component

@Component
class ScopeTokenStorage(
    private val scopeTokenRepository: ScopeTokenRepository,
    private val scopeTokenMapper: ScopeTokenMapper
) : R2DBCStorage<ScopeToken, Long> by SimpleR2DBCStorage(
    scopeTokenRepository,
    { scopeTokenMapper.map(it) }
)
