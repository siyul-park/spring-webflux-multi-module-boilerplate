package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.authentication.AuthenticateMapping
import io.github.siyual_park.auth.domain.authentication.AuthorizationStrategy
import io.github.siyual_park.auth.domain.token.TokenStorage
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@AuthenticateMapping
@Order(Ordered.LOWEST_PRECEDENCE)
class BearerAuthorizationStrategy(
    tokenStorage: TokenStorage
) : AuthorizationStrategy<ClientPrincipal>("Bearer") {
    private val tokenAuthorizationStrategy = TokenAuthorizationStrategy(tokenStorage)

    override suspend fun authenticate(credentials: String): ClientPrincipal? {
        return tokenAuthorizationStrategy.authenticate(credentials)
    }
}
