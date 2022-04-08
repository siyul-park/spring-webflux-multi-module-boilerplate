package io.github.siyual_park.application.server.gateway

import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.FluxExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient

@Component
class PingControllerGateway(
    private val client: WebTestClient
) {
    fun ping(): FluxExchangeResult<String> {
        return client.get()
            .uri("/ping")
            .exchange()
            .returnResult(String::class.java)
    }
}
