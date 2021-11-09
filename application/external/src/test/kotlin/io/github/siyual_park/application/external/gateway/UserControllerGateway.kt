package io.github.siyual_park.application.external.gateway

import io.github.siyual_park.application.external.dto.request.CreateUserRequest
import io.github.siyual_park.application.external.dto.response.UserInfo
import io.github.siyual_park.application.external.helper.AuthorizationHeaderGenerator
import io.github.siyual_park.auth.domain.Principal
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.FluxExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult

@Component
class UserControllerGateway(
    private val client: WebTestClient,
    private val authorizationHeaderGenerator: AuthorizationHeaderGenerator
) {
    fun create(request: CreateUserRequest): FluxExchangeResult<UserInfo> {
        return client.post()
            .uri("/users")
            .bodyValue(request)
            .exchange()
            .returnResult(UserInfo::class.java)
    }

    suspend fun readSelf(principal: Principal): FluxExchangeResult<UserInfo> {
        return client.get()
            .uri("/users/self")
            .header(HttpHeaders.AUTHORIZATION, authorizationHeaderGenerator.generate(principal))
            .exchange()
            .returnResult(UserInfo::class.java)
    }

    suspend fun deleteSelf(principal: Principal): FluxExchangeResult<Unit> {
        return client.delete()
            .uri("/users/self")
            .header(HttpHeaders.AUTHORIZATION, authorizationHeaderGenerator.generate(principal))
            .exchange()
            .returnResult()
    }

    suspend fun delete(userId: Long, principal: Principal): FluxExchangeResult<Unit> {
        return client.delete()
            .uri("/users/$userId")
            .header(HttpHeaders.AUTHORIZATION, authorizationHeaderGenerator.generate(principal))
            .exchange()
            .returnResult()
    }
}
