package io.github.siyual_park.auth.domain.authenticator

import io.github.siyual_park.auth.domain.token.TokenExchanger
import org.springframework.stereotype.Component

@Component
class BearerAuthorizationAuthenticateProcessor(
    private val tokenExchanger: TokenExchanger
) : AuthorizationAuthenticateProcessor<Principal> {
    override val type = "bearer"

    override suspend fun authenticate(credentials: String): Principal {
        return tokenExchanger.decode(credentials)
    }
}
