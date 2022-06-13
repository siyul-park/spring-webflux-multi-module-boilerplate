package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.authentication.AllowAllAuthenticateFilter
import io.github.siyual_park.auth.domain.authentication.Authenticator
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
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import java.time.Duration

class ClientBasedCorsCacheConfigurationSourceTest : ClientTestHelper() {
    private val tokenRepository = TokenRepository(mongoTemplate, eventPublisher = eventEmitter)

    private val tokenMapper = TokenMapper(tokenRepository, scopeTokenStorage, eventEmitter)
    private val claimEmbedder = ClaimEmbedder()

    private val tokenStorage = TokenStorage(tokenRepository, tokenMapper)
    private val tokenFactoryProvider = TokenFactoryProvider(claimEmbedder, tokenRepository, tokenMapper)

    private val authenticator = Authenticator()

    private val clientBasedCorsConfigurationSource = ClientBasedCorsConfigurationSource(
        authenticator,
        clientStorage
    )

    init {
        claimEmbedder.register(TypeMatchClaimFilter(ClientEntity::class), ClientEntityClaimEmbeddingStrategy())
        authenticator.register(AllowAllAuthenticateFilter(), BearerAuthorizationStrategy(tokenStorage))
    }

    @Test
    fun getCorsConfiguration() = blocking {
        val tokenTemplate = TokenTemplate(
            type = "test",
            limit = listOf(
                "tid" to 1
            )
        )
        val tokenFactory = tokenFactoryProvider.get(tokenTemplate)

        val client = MockCreateClientPayloadFactory.create()
            .let { clientFactory.create(it) }
        val token = tokenFactory.create(client.toPrincipal(), Duration.ofMinutes(10))

        val request = MockServerHttpRequest.options("/")
        val headers = HttpHeaders().also {
            it[HttpHeaders.AUTHORIZATION] = "Bearer ${token.signature}"
        }
        request.headers(headers)
        val exchange = MockServerWebExchange.from(request)

        val corsConfiguration = clientBasedCorsConfigurationSource.getCorsConfiguration(exchange).awaitSingleOrNull()

        assertNotNull(corsConfiguration)
        assertEquals(listOf(client.origin.toString()), corsConfiguration?.allowedOrigins)
        assertEquals(true, corsConfiguration?.allowCredentials)
    }
}
