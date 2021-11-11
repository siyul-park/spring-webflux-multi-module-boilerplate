package io.github.siyual_park.application.external.gateway

import io.github.siyual_park.application.external.dto.request.CreateUserRequest
import io.github.siyual_park.application.external.dto.response.UserInfo
import io.github.siyual_park.application.external.helper.AuthorizationHeaderGenerator
import io.github.siyual_park.auth.domain.Principal
import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.FluxExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult

class UserControllerGateway(
    private val client: WebTestClient,
    principal: Principal,
    authorizationHeaderGenerator: AuthorizationHeaderGenerator
) : AuthenticatedGateway(principal, authorizationHeaderGenerator) {
    suspend fun create(request: CreateUserRequest): FluxExchangeResult<UserInfo> {
        return client.post()
            .uri("/users")
            .header(HttpHeaders.AUTHORIZATION, getAuthorization())
            .bodyValue(request)
            .exchange()
            .returnResult(UserInfo::class.java)
    }

    suspend fun readSelf(): FluxExchangeResult<UserInfo> {
        return client.get()
            .uri("/users/self")
            .header(HttpHeaders.AUTHORIZATION, getAuthorization())
            .exchange()
            .returnResult(UserInfo::class.java)
    }

    suspend fun deleteSelf(): FluxExchangeResult<Unit> {
        return client.delete()
            .uri("/users/self")
            .header(HttpHeaders.AUTHORIZATION, getAuthorization())
            .exchange()
            .returnResult()
    }

    suspend fun delete(userId: Long): FluxExchangeResult<Unit> {
        return client.delete()
            .uri("/users/$userId")
            .header(HttpHeaders.AUTHORIZATION, getAuthorization())
            .exchange()
            .returnResult()
    }
}
