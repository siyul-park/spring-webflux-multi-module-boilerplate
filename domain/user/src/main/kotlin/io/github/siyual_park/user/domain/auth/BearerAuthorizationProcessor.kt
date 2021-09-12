package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.authenticator.AuthorizationProcessor
import io.github.siyual_park.auth.domain.token.TokenDecoder
import org.springframework.stereotype.Component

@Component
class BearerAuthorizationProcessor(
    private val tokenDecoder: TokenDecoder
) : AuthorizationProcessor<UserPrincipal> {
    override val type = "bearer"

    override suspend fun authenticate(credentials: String): UserPrincipal? {
        val claims = tokenDecoder.decode(credentials)
        if (claims["uid"] == null) {
            return null
        }

        return UserPrincipal(
            id = claims["uid"].toString(),
            scope = claims.scope.toSet()
        )
    }
}
