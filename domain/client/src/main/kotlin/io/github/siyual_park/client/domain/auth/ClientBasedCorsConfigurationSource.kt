package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.authentication.Authenticator
import io.github.siyual_park.auth.domain.authentication.AuthorizationPayload
import io.github.siyual_park.client.domain.ClientFinder
import io.github.siyual_park.client.entity.ClientEntity
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.server.ServerWebExchange

@Component
class ClientBasedCorsConfigurationSource(
    private val authenticator: Authenticator,
    private val clientFinder: ClientFinder
) : CorsConfigurationSource {
    override fun getCorsConfiguration(exchange: ServerWebExchange): CorsConfiguration? {
        return runBlocking {
            val headers = exchange.request.headers
            val authorization = headers.getFirst(HttpHeaders.AUTHORIZATION) ?: return@runBlocking null

            val token = authorization.split(" ")
            if (token.size != 2) {
                return@runBlocking null
            }

            val payload = AuthorizationPayload(token[0], token[1])
            val principal = authenticator.authenticate(payload)

            if (principal is ClientEntity) {
                val client = principal.clientId?.let { clientFinder.findById(it) } ?: return@runBlocking null
                return@runBlocking CorsConfiguration()
                    .apply {
                        addAllowedOriginPattern(client.origin.toString())
                        addAllowedHeader("*")
                        addAllowedMethod("*")
                        allowCredentials = true
                    }
            }

            return@runBlocking null
        }
    }
}
