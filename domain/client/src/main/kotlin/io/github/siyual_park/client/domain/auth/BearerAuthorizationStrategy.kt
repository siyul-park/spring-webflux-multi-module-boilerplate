package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.authentication.AuthenticateMapping
import io.github.siyual_park.auth.domain.authentication.AuthorizationPayload
import io.github.siyual_park.auth.domain.authentication.AuthorizationStrategy
import io.github.siyual_park.auth.domain.token.TokenDecoder
import org.springframework.stereotype.Component

@Component
@AuthenticateMapping(filterBy = AuthorizationPayload::class)
class BearerAuthorizationStrategy(
    private val tokenDecoder: TokenDecoder
) : AuthorizationStrategy<ClientPrincipal>("bearer") {
    override suspend fun authenticate(credentials: String): ClientPrincipal? {
        val claims = tokenDecoder.decode(credentials)
        if (claims["cid"] == null || claims["uid"] != null) {
            return null
        }

        return ClientPrincipal(
            id = claims["cid"].toString(),
            scope = claims.scope.toSet()
        )
    }
}
