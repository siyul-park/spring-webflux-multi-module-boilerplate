package io.github.siyual_park.auth.domain

import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
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
            val headers = exchange.request.headers
            val authorization = headers.getFirst(HttpHeaders.AUTHORIZATION) ?: return@mono null

            val token = authorization.split(" ")
            if (token.size != 2) {
                throw AuthenticationCredentialsNotFoundException("authorization format must to be '{type} {credentials}'.")
            }

            UsernamePasswordAuthenticationToken(token[0], token[1])
        }
    }
}
