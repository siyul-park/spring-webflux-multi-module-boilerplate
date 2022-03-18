package io.github.siyual_park.application.server.gateway

import io.github.siyual_park.application.server.dto.request.CreateClientRequest
import io.github.siyual_park.application.server.dto.response.ClientDetailInfo
import io.github.siyual_park.application.server.dto.response.ClientInfo
import io.github.siyual_park.application.server.helper.AuthorizationHeaderGenerator
import io.github.siyual_park.auth.domain.Principal
import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.FluxExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult

class ClientControllerGateway(
    private val client: WebTestClient,
    principal: Principal,
    authorizationHeaderGenerator: AuthorizationHeaderGenerator
) : AuthenticatedGateway(principal, authorizationHeaderGenerator) {
    suspend fun create(request: CreateClientRequest): FluxExchangeResult<ClientDetailInfo> {
        return client.post()
            .uri("/clients")
            .header(HttpHeaders.AUTHORIZATION, getAuthorization())
            .bodyValue(request)
            .exchange()
            .returnResult(ClientDetailInfo::class.java)
    }

    suspend fun readSelf(): FluxExchangeResult<ClientInfo> {
        return client.get()
            .uri("/clients/self")
            .header(HttpHeaders.AUTHORIZATION, getAuthorization())
            .exchange()
            .returnResult(ClientInfo::class.java)
    }
}
