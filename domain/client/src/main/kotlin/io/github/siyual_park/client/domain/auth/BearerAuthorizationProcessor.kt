package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.authentication.AuthenticateMapping
import io.github.siyual_park.auth.domain.authentication.AuthorizationPayload
import io.github.siyual_park.auth.domain.authentication.AuthorizationProcessor
import io.github.siyual_park.auth.domain.token.TokenDecoder
import org.springframework.stereotype.Component

@Component
@AuthenticateMapping(filterBy = AuthorizationPayload::class)
class BearerAuthorizationProcessor(
    private val tokenDecoder: TokenDecoder
) : AuthorizationProcessor<ClientPrincipal>("bearer") {
    override suspend fun authenticate(credentials: String): ClientPrincipal? {
        val claims = tokenDecoder.decode(credentials)
        if (claims["cid"] == null) {
            return null
        }

        return ClientPrincipal(
            id = claims["cid"].toString(),
            scope = claims.scope.toSet()
        )
    }
}
