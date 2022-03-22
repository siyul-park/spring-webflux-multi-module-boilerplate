package io.github.siyual_park.application.server.controller

import io.github.siyual_park.IntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@IntegrationTest
class PingControllerTest @Autowired constructor(
    private val client: WebTestClient
) {

    @Test
    fun `GET ping`() {
        client.get()
            .uri("/ping")
            .exchange()
            .expectStatus().isOk
            .expectBody<String>()
            .isEqualTo("pong")
    }
}
