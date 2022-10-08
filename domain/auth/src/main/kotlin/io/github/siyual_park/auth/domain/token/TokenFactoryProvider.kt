package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.repository.TokenDataRepository
import org.springframework.stereotype.Component

@Component
class TokenFactoryProvider(
    private val claimEmbedder: ClaimEmbedder,
    private val tokenDataRepository: TokenDataRepository,
    private val tokenMapper: TokenMapper,
) {
    fun get(tokenTemplate: TokenTemplate): TokenFactory {
        return TokenFactory(
            tokenTemplate,
            claimEmbedder,
            tokenDataRepository,
            tokenMapper
        )
    }
}
