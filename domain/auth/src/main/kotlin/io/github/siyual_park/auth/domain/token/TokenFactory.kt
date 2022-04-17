package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.entity.TokenData
import io.github.siyual_park.auth.repository.TokenRepository
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
class TokenFactory(
    private val claimEmbedder: ClaimEmbedder,
    private val tokenRepository: TokenRepository,
    private val tokenMapper: TokenMapper,
) {
    suspend fun create(
        principal: Principal,
        age: Duration,
        pop: Set<ScopeToken>? = null,
        push: Set<ScopeToken>? = null,
        filter: Set<ScopeToken>? = null
    ): Token {
        val baseClaim = claimEmbedder.embedding(principal)
        val scope = mutableSetOf<ScopeToken>().also { scope ->
            scope.addAll(
                principal.scope
                    .filter { filter?.contains(it) ?: true }
                    .filter { pop?.contains(it)?.let { !it } ?: true }
            )
            push?.let { scope.addAll(it) }
        }

        val claim = mutableMapOf<String, Any>()
        claim.putAll(baseClaim)
        claim["scope"] = scope.map { it.id.toString() }

        val now = Instant.now()
        val expiredAt = now.plus(age)
        val data = TokenData(
            claim,
            expiredAt
        ).let { tokenRepository.create(it) }

        return tokenMapper.map(data)
    }
}
