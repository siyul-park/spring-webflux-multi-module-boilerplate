package io.github.siyual_park.auth.spring

import io.github.siyual_park.auth.exception.InvalidAuthorizationFormatException
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class AuthenticationConverter : ServerAuthenticationConverter {
    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        return mono {
            val authorization = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION) ?: return@mono null
            val token = authorization.split(" ")
            if (token.size != 2) {
                throw InvalidAuthorizationFormatException()
            }

            UsernamePasswordAuthenticationToken(token[0], token[1])
        }
    }
}
