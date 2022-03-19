package io.github.siyual_park.application.server.gateway

import io.github.siyual_park.application.server.dto.request.CreateClientRequest
import io.github.siyual_park.application.server.dto.response.ClientDetailInfo
import io.github.siyual_park.application.server.dto.response.ClientInfo
import io.github.siyual_park.application.server.dto.response.UserInfo
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.FluxExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import java.util.Optional

@Component
class ClientControllerGateway(
    private val client: WebTestClient,
    private val gatewayAuthorization: GatewayAuthorization,
) {
    suspend fun create(request: CreateClientRequest): FluxExchangeResult<ClientDetailInfo> {
        return client.post()
            .uri("/clients")
            .header(HttpHeaders.AUTHORIZATION, gatewayAuthorization.getAuthorization())
            .bodyValue(request)
            .exchange()
            .returnResult(ClientDetailInfo::class.java)
    }

    suspend fun readAll(
        id: String? = null,
        name: String? = null,
        type: String? = null,
        origin: String? = null,
        createdAt: String? = null,
        updatedAt: String? = null,
        sort: String? = null,
        page: Int = 0,
        perPage: Int = 15,
    ): FluxExchangeResult<UserInfo> {
        return client.get()
            .uri {
                it.path("/clients")
                    .queryParamIfPresent("id", Optional.ofNullable(id))
                    .queryParamIfPresent("name", Optional.ofNullable(name))
                    .queryParamIfPresent("type", Optional.ofNullable(type))
                    .queryParamIfPresent("origin", Optional.ofNullable(origin))
                    .queryParamIfPresent("created-at", Optional.ofNullable(createdAt))
                    .queryParamIfPresent("updated-at", Optional.ofNullable(updatedAt))
                    .queryParamIfPresent("sort", Optional.ofNullable(sort))
                    .queryParamIfPresent("page", Optional.ofNullable(page))
                    .queryParamIfPresent("per-page", Optional.ofNullable(perPage))
                    .build()
            }
            .header(HttpHeaders.AUTHORIZATION, gatewayAuthorization.getAuthorization())
            .exchange()
            .returnResult(UserInfo::class.java)
    }

    suspend fun readSelf(): FluxExchangeResult<ClientInfo> {
        return client.get()
            .uri("/clients/self")
            .header(HttpHeaders.AUTHORIZATION, gatewayAuthorization.getAuthorization())
            .exchange()
            .returnResult(ClientInfo::class.java)
    }
}
