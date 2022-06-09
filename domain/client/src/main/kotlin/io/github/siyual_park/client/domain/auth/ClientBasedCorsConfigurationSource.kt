package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.authentication.Authenticator
import io.github.siyual_park.auth.domain.authentication.AuthorizationPayload
import io.github.siyual_park.client.domain.ClientStorage
import io.github.siyual_park.client.entity.ClientEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.server.ServerWebExchange

@Component
class ClientBasedCorsConfigurationSource(
    private val authenticator: Authenticator,
    private val clientStorage: ClientStorage,
) : CorsConfigurationSource {
    override fun getCorsConfiguration(exchange: ServerWebExchange): CorsConfiguration? {
        try {
            val headers = exchange.request.headers
            val authorization = headers.getFirst(HttpHeaders.AUTHORIZATION) ?: return null

            val token = authorization.split(" ")
            if (token.size != 2) {
                return null
            }

            val payload = AuthorizationPayload(token[0], token[1])
            val principal = runBlocking(Dispatchers.IO) { authenticator.authenticate(payload) }

            if (principal is ClientEntity) {
                val client = principal.clientId?.let { runBlocking(Dispatchers.IO) { clientStorage.load(it) } } ?: return null
                return CorsConfiguration()
                    .apply {
                        allowedOrigins = listOf(client.origin.toString())
                        addAllowedHeader("*")
                        addAllowedMethod("*")
                        allowCredentials = true
                    }
            }
            return null
        } catch (e: Exception) {
            return null
        }
    }
}
