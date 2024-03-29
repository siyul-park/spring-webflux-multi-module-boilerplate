package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.authentication.AuthorizationPayload
import io.github.siyual_park.auth.domain.token.ClaimEmbedder
import io.github.siyual_park.auth.domain.token.TokenMapper
import io.github.siyual_park.auth.domain.token.TokenStorage
import io.github.siyual_park.auth.domain.token.TokenTemplate
import io.github.siyual_park.auth.domain.token.TypeMatchClaimFilter
import io.github.siyual_park.auth.repository.TokenEntityRepository
import io.github.siyual_park.client.domain.ClientTestHelper
import io.github.siyual_park.client.domain.MockCreateClientPayloadFactory
import io.github.siyual_park.client.entity.ClientAssociable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Duration

class BearerAuthorizationStrategyTest : ClientTestHelper() {
    private val tokenEntityRepository = TokenEntityRepository(mongoTemplate)
    private val claimEmbedder = ClaimEmbedder()
    private val tokenMapper = TokenMapper(tokenEntityRepository, scopeTokenStorage)
    private val tokenStorage = TokenStorage(claimEmbedder, tokenEntityRepository, tokenMapper)

    private val bearerAuthorizationStrategy = BearerAuthorizationStrategy(tokenStorage)

    init {
        claimEmbedder.register(TypeMatchClaimFilter(ClientAssociable::class), ClientAssociableClaimEmbeddingStrategy())
    }

    @Test
    fun authenticate() = blocking {
        val template = TokenTemplate(type = "test", age = Duration.ofMinutes(30))
        val tokenFactory = tokenStorage.createFactory(template)

        val client = MockCreateClientPayloadFactory.create()
            .let { clientStorage.save(it) }
        val principal = client.toPrincipal()

        val token = tokenFactory.create(principal)

        assertNull(bearerAuthorizationStrategy.authenticate(AuthorizationPayload("invalid", token.signature)))
        assertEquals(principal.copy(id = token.id), bearerAuthorizationStrategy.authenticate(AuthorizationPayload("bearer", token.signature)))
    }
}
