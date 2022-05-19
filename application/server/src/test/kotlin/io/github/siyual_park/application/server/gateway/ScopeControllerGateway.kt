package io.github.siyual_park.application.server.gateway

import io.github.siyual_park.application.server.dto.request.CreateScopeTokenRequest
import io.github.siyual_park.application.server.dto.request.UpdateScopeTokenRequest
import io.github.siyual_park.application.server.dto.response.ScopeTokenInfo
import io.github.siyual_park.ulid.ULID
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.FluxExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import java.util.Optional

@Component
class ScopeControllerGateway(
    private val client: WebTestClient,
    private val gatewayAuthorization: GatewayAuthorization,
) {
    suspend fun create(request: CreateScopeTokenRequest): FluxExchangeResult<ScopeTokenInfo> {
        return client.post()
            .uri("/scope")
            .header(HttpHeaders.AUTHORIZATION, gatewayAuthorization.getAuthorization())
            .bodyValue(request)
            .exchange()
            .returnResult(ScopeTokenInfo::class.java)
    }

    suspend fun readAll(
        id: String? = null,
        name: String? = null,
        createdAt: String? = null,
        updatedAt: String? = null,
        sort: String? = null,
        page: Int = 0,
        perPage: Int = 15,
    ): FluxExchangeResult<ScopeTokenInfo> {
        return client.get()
            .uri {
                it.path("/scope")
                    .queryParamIfPresent("id", Optional.ofNullable(id))
                    .queryParamIfPresent("name", Optional.ofNullable(name))
                    .queryParamIfPresent("created_at", Optional.ofNullable(createdAt))
                    .queryParamIfPresent("updated_at", Optional.ofNullable(updatedAt))
                    .queryParamIfPresent("sort", Optional.ofNullable(sort))
                    .queryParamIfPresent("page", Optional.ofNullable(page))
                    .queryParamIfPresent("per_page", Optional.ofNullable(perPage))
                    .build()
            }
            .header(HttpHeaders.AUTHORIZATION, gatewayAuthorization.getAuthorization())
            .exchange()
            .returnResult(ScopeTokenInfo::class.java)
    }

    suspend fun read(scopeId: ULID): FluxExchangeResult<ScopeTokenInfo> {
        return client.get()
            .uri("/scope/$scopeId")
            .header(HttpHeaders.AUTHORIZATION, gatewayAuthorization.getAuthorization())
            .exchange()
            .returnResult(ScopeTokenInfo::class.java)
    }

    suspend fun update(scopeId: ULID, request: UpdateScopeTokenRequest): FluxExchangeResult<ScopeTokenInfo> {
        return client.patch()
            .uri("/scope/$scopeId")
            .header(HttpHeaders.AUTHORIZATION, gatewayAuthorization.getAuthorization())
            .bodyValue(request)
            .exchange()
            .returnResult(ScopeTokenInfo::class.java)
    }

    suspend fun delete(scopeId: ULID): FluxExchangeResult<Unit> {
        return client.delete()
            .uri("/scope/$scopeId")
            .header(HttpHeaders.AUTHORIZATION, gatewayAuthorization.getAuthorization())
            .exchange()
            .returnResult()
    }
}
