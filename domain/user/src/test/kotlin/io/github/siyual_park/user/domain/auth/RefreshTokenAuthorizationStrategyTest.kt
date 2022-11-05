package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.authentication.RefreshTokenPayload
import io.github.siyual_park.auth.domain.token.ClaimEmbedder
import io.github.siyual_park.auth.domain.token.TokenMapper
import io.github.siyual_park.auth.domain.token.TokenStorage
import io.github.siyual_park.auth.domain.token.TokenTemplate
import io.github.siyual_park.auth.domain.token.TypeMatchClaimFilter
import io.github.siyual_park.auth.repository.TokenEntityRepository
import io.github.siyual_park.user.domain.MockCreateUserPayloadFactory
import io.github.siyual_park.user.domain.UserTestHelper
import io.github.siyual_park.user.entity.UserAssociable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration

class RefreshTokenAuthorizationStrategyTest : UserTestHelper() {
    private val tokenEntityRepository = TokenEntityRepository(mongoTemplate)

    private val tokenMapper = TokenMapper(tokenEntityRepository, scopeTokenStorage)
    private val claimEmbedder = ClaimEmbedder()
    private val tokenStorage = TokenStorage(claimEmbedder, tokenEntityRepository, tokenMapper)

    private val refreshTokenAuthorizationStrategy = RefreshTokenAuthorizationStrategy(tokenStorage)

    init {
        claimEmbedder.register(TypeMatchClaimFilter(UserAssociable::class), UserAssociableClaimEmbeddingStrategy())
    }

    @Test
    fun authenticate() = blocking {
        val template = TokenTemplate(type = "test", age = Duration.ofMinutes(30))
        val tokenFactory = tokenStorage.createFactory(template)

        val user = MockCreateUserPayloadFactory.create()
            .let { userStorage.save(it) }
        val principal = user.toPrincipal()

        val token = tokenFactory.create(principal)

        assertEquals(
            principal.copy(id = token.id),
            refreshTokenAuthorizationStrategy.authenticate(RefreshTokenPayload(token.signature))
        )
    }
}
