package io.github.siyual_park.application.external.gateway

import io.github.siyual_park.application.external.dto.request.CreateUserRequest
import io.github.siyual_park.application.external.dto.response.CreateUserResponse
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.FluxExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient

@Component
class UserControllerGateway(
    private val client: WebTestClient
) {
    fun create(request: CreateUserRequest): FluxExchangeResult<CreateUserResponse> {
        return client.post()
            .uri("/users")
            .bodyValue(request)
            .exchange()
            .returnResult(CreateUserResponse::class.java)
    }
}
