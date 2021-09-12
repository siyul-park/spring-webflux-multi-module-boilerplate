package io.github.siyual_park.application.external.helper

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.token.TokenIssuer
import org.springframework.stereotype.Component

@Component
class AuthorizationHeaderGenerator(
    private val tokenIssuer: TokenIssuer
) {
    suspend fun generate(principal: Principal): String {
        val tokens = tokenIssuer.issue(principal)
        return "${tokens.tokenType} ${tokens.accessToken}"
    }
}
