package io.github.siyual_park.auth.domain

import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import org.springframework.stereotype.Component

@Component
class ScopeTokenGenerator(
    private val scopeTokenRepository: ScopeTokenRepository
) {
    private val scopeTokens = mutableListOf<ScopeToken>()

    fun register(scopeToken: ScopeToken): ScopeTokenGenerator {
        scopeTokens.add(scopeToken)
        return this
    }

    suspend fun generate() {
        scopeTokenRepository.createAll(scopeTokens)
    }
}
