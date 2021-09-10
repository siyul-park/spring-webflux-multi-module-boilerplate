package io.github.siyual_park.application.external.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@ControllerTest
class PingControllerTest @Autowired constructor(
    private val client: WebTestClient
) {

    @Test
    fun testPing() {
        client.get()
            .uri("/ping")
            .exchange()
            .expectStatus().isOk
            .expectBody<String>().isEqualTo("pong")
    }
}
