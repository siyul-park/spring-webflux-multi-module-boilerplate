package io.github.siyual_park.application.server.gateway.factory

import io.github.siyual_park.application.server.gateway.ClientControllerGateway
import io.github.siyual_park.application.server.helper.AuthorizationHeaderGenerator
import io.github.siyual_park.auth.domain.Principal
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.WebTestClient

@Component
class ClientControllerGatewayFactory(
    private val client: WebTestClient,
    private val authorizationHeaderGenerator: AuthorizationHeaderGenerator
) {
    fun create(principal: Principal) = ClientControllerGateway(client, principal, authorizationHeaderGenerator)
}
