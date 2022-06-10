package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.authentication.Authenticator
import io.github.siyual_park.auth.domain.authentication.AuthorizationPayload
import io.github.siyual_park.auth.domain.cors.CorsConfigurationSource
import io.github.siyual_park.client.domain.ClientStorage
import io.github.siyual_park.client.entity.ClientEntity
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class ClientBasedCorsConfigurationSource(
    private val authenticator: Authenticator,
    private val clientStorage: ClientStorage,
) : CorsConfigurationSource {
    override fun getCorsConfiguration(exchange: ServerWebExchange): Mono<CorsConfiguration> {
        return mono {
            try {
                val headers = exchange.request.headers
                val authorization = headers.getFirst(HttpHeaders.AUTHORIZATION) ?: return@mono null

                val token = authorization.split(" ")
                if (token.size != 2) {
                    return@mono null
                }

                val payload = AuthorizationPayload(token[0], token[1])
                val principal = authenticator.authenticate(payload)
                if (principal !is ClientEntity) {
                    return@mono null
                }

                val client = principal.clientId?.let { clientStorage.load(it) } ?: return@mono null

                CorsConfiguration()
                    .apply {
                        allowedOrigins = listOf(client.origin.toString())
                        addAllowedHeader("*")
                        addAllowedMethod("*")
                        allowCredentials = true
                    }
            } catch (e: Exception) {
                null
            }
        }
    }
}
