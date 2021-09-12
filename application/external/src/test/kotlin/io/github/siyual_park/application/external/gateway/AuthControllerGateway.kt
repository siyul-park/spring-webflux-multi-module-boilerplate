package io.github.siyual_park.application.external.gateway

import io.github.siyual_park.application.external.dto.request.CreateTokenRequest
import io.github.siyual_park.application.external.dto.response.CreateTokenResponse
import io.github.siyual_park.application.external.encoder.FormDataEncoder
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.FluxExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient

@Component
class AuthControllerGateway(
    private val client: WebTestClient,
    private val formDataEncoder: FormDataEncoder
) {
    fun create(request: CreateTokenRequest): FluxExchangeResult<CreateTokenResponse> {
        return client.post()
            .uri("/token")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body(formDataEncoder.encode(request))
            .exchange()
            .returnResult(CreateTokenResponse::class.java)
    }
}
