package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.authentication.RefreshTokenPayload
import io.github.siyual_park.auth.domain.token.ClaimEmbedder
import io.github.siyual_park.auth.domain.token.TokenFactoryProvider
import io.github.siyual_park.auth.domain.token.TokenMapper
import io.github.siyual_park.auth.domain.token.TokenStorage
import io.github.siyual_park.auth.domain.token.TokenTemplate
import io.github.siyual_park.auth.domain.token.TypeMatchClaimFilter
import io.github.siyual_park.auth.repository.TokenRepository
import io.github.siyual_park.client.domain.ClientTestHelper
import io.github.siyual_park.client.domain.MockCreateClientPayloadFactory
import io.github.siyual_park.client.entity.ClientEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration

class RefreshTokenAuthorizationStrategyTest : ClientTestHelper() {
    private val tokenRepository = TokenRepository(mongoTemplate)

    private val tokenMapper = TokenMapper(tokenRepository, scopeTokenStorage)
    private val claimEmbedder = ClaimEmbedder()

    private val tokenStorage = TokenStorage(tokenRepository, tokenMapper)
    private val tokenFactoryProvider = TokenFactoryProvider(claimEmbedder, tokenRepository, tokenMapper)

    private val refreshTokenAuthorizationStrategy = RefreshTokenAuthorizationStrategy(tokenStorage)

    init {
        claimEmbedder.register(TypeMatchClaimFilter(ClientEntity::class), ClientEntityClaimEmbeddingStrategy())
    }

    @Test
    fun authenticate() = blocking {
        val template = TokenTemplate(type = "test")
        val tokenFactory = tokenFactoryProvider.get(template)

        val client = MockCreateClientPayloadFactory.create()
            .let { clientFactory.create(it) }
        val principal = client.toPrincipal()

        val token = tokenFactory.create(principal, Duration.ofMinutes(30))

        assertEquals(
            principal.copy(id = token.id),
            refreshTokenAuthorizationStrategy.authenticate(RefreshTokenPayload(token.signature))
        )
    }
}
