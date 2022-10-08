package io.github.siyual_park.application.server.controller

import io.github.siyual_park.IntegrationTest
import io.github.siyual_park.application.server.gateway.CacheControllerGateway
import io.github.siyual_park.application.server.gateway.GatewayAuthorization
import io.github.siyual_park.client.domain.ClientFactory
import io.github.siyual_park.client.domain.MockCreateClientPayloadFactory
import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

@IntegrationTest
class CacheControllerTest @Autowired constructor(
    private val gatewayAuthorization: GatewayAuthorization,
    private val cacheControllerGateway: CacheControllerGateway,
    private val clientFactory: ClientFactory,
) : CoroutineTestHelper() {

    @Test
    fun `GET cache_status, status = 200`() = blocking {
        val principal = MockCreateClientPayloadFactory.create()
            .let { clientFactory.create(it).toPrincipal() }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("cache.status:read")
        )

        val response = cacheControllerGateway.status()

        assertEquals(HttpStatus.OK, response.status)
    }
}
