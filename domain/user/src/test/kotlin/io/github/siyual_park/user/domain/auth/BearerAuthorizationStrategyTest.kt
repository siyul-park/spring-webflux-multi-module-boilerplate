package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.authentication.AuthorizationPayload
import io.github.siyual_park.auth.domain.token.ClaimEmbedder
import io.github.siyual_park.auth.domain.token.TokenFactoryProvider
import io.github.siyual_park.auth.domain.token.TokenMapper
import io.github.siyual_park.auth.domain.token.TokenStorage
import io.github.siyual_park.auth.domain.token.TokenTemplate
import io.github.siyual_park.auth.repository.TokenRepository
import io.github.siyual_park.user.domain.UserTestHelper
import io.github.siyual_park.user.dummy.DummyCreateUserPayload
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Duration

class BearerAuthorizationStrategyTest : UserTestHelper() {
    private val tokenRepository = TokenRepository(mongoTemplate, eventEmitter)

    private val tokenMapper = TokenMapper(tokenRepository, scopeTokenStorage, eventEmitter)
    private val claimEmbedder = ClaimEmbedder()

    private val tokenStorage = TokenStorage(tokenRepository, tokenMapper)
    private val tokenFactoryProvider = TokenFactoryProvider(claimEmbedder, tokenRepository, tokenMapper)

    private val bearerAuthorizationStrategy = BearerAuthorizationStrategy(tokenStorage)

    init {
        claimEmbedder.register(UserPrincipal::class, UserPrincipalClaimEmbeddingStrategy())
    }

    @Test
    fun authenticate() = blocking {
        val template = TokenTemplate(type = "test")
        val tokenFactory = tokenFactoryProvider.get(template)

        val user = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }
        val principal = user.toPrincipal()

        val token = tokenFactory.create(principal, Duration.ofMinutes(30))

        assertNull(bearerAuthorizationStrategy.authenticate(AuthorizationPayload("invalid", token.signature)))
        assertEquals(principal, bearerAuthorizationStrategy.authenticate(AuthorizationPayload("bearer", token.signature)))
    }
}
