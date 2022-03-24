package io.github.siyual_park.application.server.gateway

import io.github.siyual_park.application.server.dto.request.CreateUserRequest
import io.github.siyual_park.application.server.dto.request.GrantScopeRequest
import io.github.siyual_park.application.server.dto.request.UpdateUserRequest
import io.github.siyual_park.application.server.dto.response.ScopeTokenInfo
import io.github.siyual_park.application.server.dto.response.UserInfo
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.FluxExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import java.util.Optional

@Component
class UserControllerGateway(
    private val client: WebTestClient,
    private val gatewayAuthorization: GatewayAuthorization,
) {
    suspend fun create(request: CreateUserRequest): FluxExchangeResult<UserInfo> {
        return client.post()
            .uri("/users")
            .header(HttpHeaders.AUTHORIZATION, gatewayAuthorization.getAuthorization())
            .bodyValue(request)
            .exchange()
            .returnResult(UserInfo::class.java)
    }

    suspend fun readAll(
        id: String? = null,
        name: String? = null,
        createdAt: String? = null,
        updatedAt: String? = null,
        sort: String? = null,
        page: Int = 0,
        perPage: Int = 15,
    ): FluxExchangeResult<UserInfo> {
        return client.get()
            .uri {
                it.path("/users")
                    .queryParamIfPresent("id", Optional.ofNullable(id))
                    .queryParamIfPresent("name", Optional.ofNullable(name))
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

    suspend fun readSelf(): FluxExchangeResult<UserInfo> {
        return client.get()
            .uri("/users/self")
            .header(HttpHeaders.AUTHORIZATION, gatewayAuthorization.getAuthorization())
            .exchange()
            .returnResult(UserInfo::class.java)
    }

    suspend fun read(userId: Long): FluxExchangeResult<UserInfo> {
        return client.get()
            .uri("/users/$userId")
            .header(HttpHeaders.AUTHORIZATION, gatewayAuthorization.getAuthorization())
            .exchange()
            .returnResult(UserInfo::class.java)
    }

    suspend fun update(userId: Long, request: UpdateUserRequest): FluxExchangeResult<UserInfo> {
        return client.patch()
            .uri("/users/$userId")
            .header(HttpHeaders.AUTHORIZATION, gatewayAuthorization.getAuthorization())
            .bodyValue(request)
            .exchange()
            .returnResult(UserInfo::class.java)
    }

    suspend fun delete(userId: Long): FluxExchangeResult<Unit> {
        return client.delete()
            .uri("/users/$userId")
            .header(HttpHeaders.AUTHORIZATION, gatewayAuthorization.getAuthorization())
            .exchange()
            .returnResult()
    }

    suspend fun readScope(userId: Long): FluxExchangeResult<ScopeTokenInfo> {
        return client.get()
            .uri("/users/$userId/scope")
            .header(HttpHeaders.AUTHORIZATION, gatewayAuthorization.getAuthorization())
            .exchange()
            .returnResult(ScopeTokenInfo::class.java)
    }

    suspend fun grantScope(userId: Long, request: GrantScopeRequest): FluxExchangeResult<ScopeTokenInfo> {
        return client.post()
            .uri("/users/$userId/scope")
            .header(HttpHeaders.AUTHORIZATION, gatewayAuthorization.getAuthorization())
            .bodyValue(request)
            .exchange()
            .returnResult(ScopeTokenInfo::class.java)
    }

    suspend fun revokeScope(clientId: Long, scopeId: Long): FluxExchangeResult<Unit> {
        return client.delete()
            .uri("/users/$clientId/scope/$scopeId")
            .header(HttpHeaders.AUTHORIZATION, gatewayAuthorization.getAuthorization())
            .exchange()
            .returnResult()
    }
}
