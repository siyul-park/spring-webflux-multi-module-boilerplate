package io.github.siyual_park.application.server.gateway

import io.github.siyual_park.application.server.dto.request.UpdateUserCredentialRequest
import io.github.siyual_park.application.server.dto.response.UserCredentialInfo
import io.github.siyual_park.ulid.ULID
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.FluxExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient

@Component
class UserCredentialControllerGateway(
    private val client: WebTestClient,
    private val gatewayAuthorization: GatewayAuthorization,
) {

    suspend fun update(userId: ULID, request: UpdateUserCredentialRequest): FluxExchangeResult<UserCredentialInfo> {
        return client.patch()
            .uri("/users/$userId/credential")
            .header(HttpHeaders.AUTHORIZATION, gatewayAuthorization.getAuthorization())
            .bodyValue(request)
            .exchange()
            .returnResult(UserCredentialInfo::class.java)
    }
}
