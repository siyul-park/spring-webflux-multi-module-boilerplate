package io.github.siyual_park.application.server.gateway

import io.github.siyual_park.application.server.helper.AuthorizationHeaderGenerator
import io.github.siyual_park.auth.domain.Principal
import org.springframework.stereotype.Component

@Component
class GatewayAuthorization(
    private val authorizationHeaderGenerator: AuthorizationHeaderGenerator
) {
    private var principal: Principal? = null

    fun setPrincipal(principal: Principal) {
        this.principal = principal
    }

    suspend fun getAuthorization(): String {
        return principal?.let { authorizationHeaderGenerator.generate(it) } ?: throw RuntimeException()
    }
}
