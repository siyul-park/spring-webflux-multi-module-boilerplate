package io.github.siyual_park.application.server.gateway

import io.github.siyual_park.application.server.dto.request.CreateTokenRequest
import io.github.siyual_park.application.server.dto.response.PrincipalInfo
import io.github.siyual_park.application.server.dto.response.TokenInfo
import io.github.siyual_park.application.server.encoder.FormDataEncoder
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.FluxExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult

@Component
class AuthControllerGateway(
    private val client: WebTestClient,
    private val gatewayAuthorization: GatewayAuthorization,
    private val formDataEncoder: FormDataEncoder
) {
    fun createToken(request: CreateTokenRequest): FluxExchangeResult<TokenInfo> {
        return client.post()
            .uri("/token")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body(formDataEncoder.encode(request))
            .exchange()
            .returnResult(TokenInfo::class.java)
    }

    suspend fun readSelf(): FluxExchangeResult<PrincipalInfo> {
        return client.get()
            .uri("/self")
            .header(HttpHeaders.AUTHORIZATION, gatewayAuthorization.getAuthorization())
            .exchange()
            .returnResult(PrincipalInfo::class.java)
    }

    suspend fun deleteSelf(): FluxExchangeResult<Unit> {
        return client.delete()
            .uri("/self")
            .header(HttpHeaders.AUTHORIZATION, gatewayAuthorization.getAuthorization())
            .exchange()
            .returnResult()
    }
}
