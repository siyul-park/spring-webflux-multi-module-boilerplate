package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.authentication.AllowAllAuthenticateFilter
import io.github.siyual_park.auth.domain.authentication.Authenticator
import io.github.siyual_park.auth.domain.token.ClaimEmbedder
import io.github.siyual_park.auth.domain.token.TokenMapper
import io.github.siyual_park.auth.domain.token.TokenStorage
import io.github.siyual_park.auth.domain.token.TokenTemplate
import io.github.siyual_park.auth.domain.token.TypeMatchClaimFilter
import io.github.siyual_park.auth.repository.TokenEntityRepository
import io.github.siyual_park.client.domain.ClientTestHelper
import io.github.siyual_park.client.domain.MockCreateClientPayloadFactory
import io.github.siyual_park.client.entity.ClientAssociable
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import java.time.Duration

class ClientBasedCorsCacheConfigurationSourceTest : ClientTestHelper() {
    private val tokenEntityRepository = TokenEntityRepository(mongoTemplate)

    private val tokenMapper = TokenMapper(tokenEntityRepository, scopeTokenStorage)
    private val claimEmbedder = ClaimEmbedder()

    private val tokenStorage = TokenStorage(claimEmbedder, tokenEntityRepository, tokenMapper)

    private val authenticator = Authenticator()

    private val clientBasedCorsConfigurationSource = ClientBasedCorsConfigurationSource(
        authenticator,
        this.clientStorage
    )

    init {
        claimEmbedder.register(TypeMatchClaimFilter(ClientAssociable::class), ClientAssociableClaimEmbeddingStrategy())
        authenticator.register(AllowAllAuthenticateFilter(), BearerAuthorizationStrategy(tokenStorage))
    }

    @Test
    fun getCorsConfiguration() = blocking {
        val tokenTemplate = TokenTemplate(
            type = "test",
            age = Duration.ofMinutes(10),
            limit = listOf(
                "tid" to 1
            )
        )
        val tokenFactory = tokenStorage.createFactory(tokenTemplate)

        val client = MockCreateClientPayloadFactory.create()
            .let { clientStorage.save(it) }
        val token = tokenFactory.create(client.toPrincipal())

        val request = MockServerHttpRequest.options("/")
        val headers = HttpHeaders().also {
            it[HttpHeaders.AUTHORIZATION] = "Bearer ${token.signature}"
        }
        request.headers(headers)
        val exchange = MockServerWebExchange.from(request)

        val corsConfiguration = clientBasedCorsConfigurationSource.getCorsConfiguration(exchange).awaitSingleOrNull()

        assertNotNull(corsConfiguration)
        assertEquals(client.origins.toString(), corsConfiguration?.allowedOrigins?.toString())
        assertEquals(true, corsConfiguration?.allowCredentials)
    }
}
