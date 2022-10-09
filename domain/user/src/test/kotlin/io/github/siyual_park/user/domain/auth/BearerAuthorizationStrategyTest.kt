package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.authentication.AuthorizationPayload
import io.github.siyual_park.auth.domain.token.ClaimEmbedder
import io.github.siyual_park.auth.domain.token.TokenMapper
import io.github.siyual_park.auth.domain.token.TokenStorage
import io.github.siyual_park.auth.domain.token.TokenTemplate
import io.github.siyual_park.auth.domain.token.TypeMatchClaimFilter
import io.github.siyual_park.auth.repository.TokenDataRepository
import io.github.siyual_park.user.domain.MockCreateUserPayloadFactory
import io.github.siyual_park.user.domain.UserTestHelper
import io.github.siyual_park.user.entity.UserEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Duration

class BearerAuthorizationStrategyTest : UserTestHelper() {
    private val tokenDataRepository = TokenDataRepository(mongoTemplate)

    private val claimEmbedder = ClaimEmbedder()
    private val tokenMapper = TokenMapper(tokenDataRepository, scopeTokenStorage)
    private val tokenStorage = TokenStorage(claimEmbedder, tokenDataRepository, tokenMapper)

    private val authorizationStrategy = BearerAuthorizationStrategy(tokenStorage)

    init {
        claimEmbedder.register(TypeMatchClaimFilter(UserEntity::class), UserEntityClaimEmbeddingStrategy())
    }

    @Test
    fun authenticate() = blocking {
        val template = TokenTemplate(type = "test", age = Duration.ofMinutes(30))
        val tokenFactory = tokenStorage.createFactory(template)

        val user = MockCreateUserPayloadFactory.create()
            .let { userStorage.save(it) }
        val principal = user.toPrincipal()

        val token = tokenFactory.create(principal)

        assertNull(authorizationStrategy.authenticate(AuthorizationPayload("invalid", token.signature)))
        assertEquals(principal.copy(id = token.id), authorizationStrategy.authenticate(AuthorizationPayload("bearer", token.signature)))
    }
}
