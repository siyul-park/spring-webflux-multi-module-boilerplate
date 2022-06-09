package io.github.siyual_park.auth.domain.cors

import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

interface CorsConfigurationSource {
    fun getCorsConfiguration(exchange: ServerWebExchange): Mono<CorsConfiguration>
}
