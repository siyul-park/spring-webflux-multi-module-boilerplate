package io.github.siyual_park.application.server.gateway

import io.github.siyual_park.application.server.dto.response.CacheStatusInfo
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.FluxExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient

@Component
class CacheControllerGateway(
    private val client: WebTestClient,
    private val gatewayAuthorization: GatewayAuthorization,
) {

    suspend fun status(): FluxExchangeResult<Map<String, CacheStatusInfo>> {
        return client.get()
            .uri("/cache/status")
            .header(HttpHeaders.AUTHORIZATION, gatewayAuthorization.getAuthorization())
            .exchange()
            .returnResult(object : ParameterizedTypeReference<Map<String, CacheStatusInfo>>() {})
    }
}
