package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.authenticator.AuthenticateMapping
import io.github.siyual_park.auth.domain.authenticator.AuthorizationPayload
import io.github.siyual_park.auth.domain.authenticator.AuthorizationProcessor
import io.github.siyual_park.auth.domain.token.TokenDecoder
import org.springframework.stereotype.Component

@Component
@AuthenticateMapping(filterBy = AuthorizationPayload::class)
class BearerAuthorizationProcessor(
    private val tokenDecoder: TokenDecoder
) : AuthorizationProcessor<UserPrincipal>("bearer") {
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
