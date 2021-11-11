package io.github.siyual_park.application.external.gateway

import io.github.siyual_park.application.external.helper.AuthorizationHeaderGenerator
import io.github.siyual_park.auth.domain.Principal

open class AuthenticatedGateway(
    private val principal: Principal,
    private val authorizationHeaderGenerator: AuthorizationHeaderGenerator
) {
    suspend fun getAuthorization(): String {
        return authorizationHeaderGenerator.generate(principal)
    }
}
