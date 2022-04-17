package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.authentication.AuthenticateMapping
import io.github.siyual_park.auth.domain.authentication.AuthorizationPayload
import io.github.siyual_park.auth.domain.authentication.AuthorizationStrategy
import io.github.siyual_park.auth.domain.token.TokenStorage
import io.github.siyual_park.persistence.loadOrFail
import kotlinx.coroutines.flow.toSet
import org.springframework.stereotype.Component

@Component
@AuthenticateMapping(filterBy = AuthorizationPayload::class)
class BearerAuthorizationStrategy(
    private val tokenStorage: TokenStorage
) : AuthorizationStrategy<ClientPrincipal>("bearer") {
    override suspend fun authenticate(credentials: String): ClientPrincipal? {
        val token = tokenStorage.loadOrFail(credentials)
        val claims = token.claims
        if (claims["cid"] == null || claims["uid"] != null) {
            return null
        }

        return ClientPrincipal(
            id = claims["cid"].toString(),
            scope = token.getScope(deep = true).toSet()
        )
    }
}
