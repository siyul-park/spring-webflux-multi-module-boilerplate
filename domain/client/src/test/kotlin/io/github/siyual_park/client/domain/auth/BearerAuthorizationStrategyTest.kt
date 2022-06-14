package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.authentication.AuthorizationPayload
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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Duration

class BearerAuthorizationStrategyTest : ClientTestHelper() {

    private val tokenRepository = TokenRepository(mongoTemplate, eventPublisher = eventEmitter)

    private val tokenMapper = TokenMapper(tokenRepository, scopeTokenStorage, eventEmitter)
    private val tokenStorage = TokenStorage(tokenRepository, tokenMapper)

    private val claimEmbedder = ClaimEmbedder()
    private val tokenFactoryProvider = TokenFactoryProvider(claimEmbedder, tokenRepository, tokenMapper)

    private val bearerAuthorizationStrategy = BearerAuthorizationStrategy(tokenStorage)

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

        assertNull(bearerAuthorizationStrategy.authenticate(AuthorizationPayload("invalid", token.signature)))
        assertEquals(principal.copy(id = token.id), bearerAuthorizationStrategy.authenticate(AuthorizationPayload("bearer", token.signature)))
    }
}
