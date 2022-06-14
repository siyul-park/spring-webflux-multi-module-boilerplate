package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.authentication.AuthenticateMapping
import io.github.siyual_park.auth.domain.authentication.AuthorizationStrategy
import io.github.siyual_park.auth.domain.token.TokenStorage
import io.github.siyual_park.persistence.loadOrFail
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.flow.toSet
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@AuthenticateMapping
@Order(Ordered.LOWEST_PRECEDENCE)
class BearerAuthorizationStrategy(
    private val tokenStorage: TokenStorage
) : AuthorizationStrategy<ClientPrincipal>("Bearer") {
    override suspend fun authenticate(credentials: String): ClientPrincipal? {
        val token = tokenStorage.loadOrFail(credentials)
        if (token["cid"] == null) {
            return null
        }

        return ClientPrincipal(
            id = token.id,
            clientId = ULID.fromString(token["cid"].toString()),
            scope = token.getScope(deep = true).toSet()
        )
    }
}
