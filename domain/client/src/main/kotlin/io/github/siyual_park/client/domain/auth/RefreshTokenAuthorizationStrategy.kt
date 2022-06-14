package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.authentication.AuthenticateMapping
import io.github.siyual_park.auth.domain.authentication.AuthenticateStrategy
import io.github.siyual_park.auth.domain.authentication.RefreshTokenPayload
import io.github.siyual_park.auth.domain.token.TokenStorage
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@AuthenticateMapping
@Order(Ordered.LOWEST_PRECEDENCE)
class RefreshTokenAuthorizationStrategy(
    tokenStorage: TokenStorage
) : AuthenticateStrategy<RefreshTokenPayload, ClientPrincipal> {
    override val clazz = RefreshTokenPayload::class

    private val tokenAuthorizationStrategy = TokenAuthorizationStrategy(tokenStorage)

    override suspend fun authenticate(payload: RefreshTokenPayload): ClientPrincipal? {
        return tokenAuthorizationStrategy.authenticate(payload.refreshToken)
    }
}
