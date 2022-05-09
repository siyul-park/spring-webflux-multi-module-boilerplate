package io.github.siyual_park.application.server.controller

import io.github.siyual_park.IntegrationTest
import io.github.siyual_park.application.server.gateway.PingControllerGateway
import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import kotlinx.coroutines.reactive.awaitSingle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

@IntegrationTest
class PingControllerTest @Autowired constructor(
    private val pingControllerGateway: PingControllerGateway,
) : CoroutineTestHelper() {
    @Test
    fun `GET ping, status = 201`() = blocking {
        val response = pingControllerGateway.ping()
        assertEquals(HttpStatus.OK, response.status)

        val body = response.responseBody.awaitSingle()
        assertEquals("pong", body)
    }
}
