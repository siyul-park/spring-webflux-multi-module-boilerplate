package io.github.siyual_park.auth.domain.authenticator.authenricate_processor

import io.github.siyual_park.auth.domain.principal.Principal
import io.github.siyual_park.auth.domain.token.TokenExchanger
import org.springframework.stereotype.Component

@Component
class BearerAuthorizationProcessor(
    private val tokenExchanger: TokenExchanger
) : AuthorizationProcessor<Principal> {
    override val type = "bearer"

    override suspend fun authenticate(credentials: String): Principal {
        return tokenExchanger.decode(credentials)
    }
}
