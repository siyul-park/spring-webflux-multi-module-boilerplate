package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.authentication.AuthenticateMapping
import io.github.siyual_park.auth.domain.authentication.AuthorizationPayload
import io.github.siyual_park.auth.domain.authentication.AuthorizationStrategy
import io.github.siyual_park.auth.domain.token.TokenParser
import io.github.siyual_park.ulid.ULID
import org.springframework.stereotype.Component

@Component
@AuthenticateMapping(filterBy = AuthorizationPayload::class)
class BearerAuthorizationStrategy(
    private val tokenParser: TokenParser
) : AuthorizationStrategy<UserPrincipal>("bearer") {
    override suspend fun authenticate(credentials: String): UserPrincipal? {
        val claims = tokenParser.decode(credentials)
        if (claims["uid"] == null) {
            return null
        }

        return UserPrincipal(
            id = claims["uid"].toString(),
            clientId = claims["cid"]?.toString()?.let { ULID.fromString(it) },
            scope = claims.scope.toSet()
        )
    }
}
