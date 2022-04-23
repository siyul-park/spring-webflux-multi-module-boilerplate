package io.github.siyual_park.application.server.gateway

import io.github.siyual_park.application.server.dto.response.UserContactInfo
import io.github.siyual_park.ulid.ULID
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.FluxExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient

@Component
class UserContactControllerGateway(
    private val client: WebTestClient,
    private val gatewayAuthorization: GatewayAuthorization,
) {

    suspend fun read(userId: ULID): FluxExchangeResult<UserContactInfo> {
        return client.get()
            .uri("/users/$userId/contact")
            .header(HttpHeaders.AUTHORIZATION, gatewayAuthorization.getAuthorization())
            .exchange()
            .returnResult(UserContactInfo::class.java)
    }
}
