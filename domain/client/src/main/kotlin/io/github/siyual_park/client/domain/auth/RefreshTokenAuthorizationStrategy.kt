package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.authentication.AuthenticateMapping
import io.github.siyual_park.auth.domain.authentication.AuthenticateStrategy
import io.github.siyual_park.auth.domain.authentication.RefreshTokenPayload
import io.github.siyual_park.auth.domain.token.TokenStorage
import kotlinx.coroutines.flow.toSet
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@AuthenticateMapping
@Order(Ordered.LOWEST_PRECEDENCE)
class RefreshTokenAuthorizationStrategy(
    private val tokenStorage: TokenStorage
) : AuthenticateStrategy<RefreshTokenPayload, ClientPrincipal> {
    override val clazz = RefreshTokenPayload::class

    override suspend fun authenticate(payload: RefreshTokenPayload): ClientPrincipal? {
        val token = tokenStorage.loadOrFail(payload.refreshToken)
        if (token["cid"] == null) {
            return null
        }

        return ClientPrincipal(
            id = token["cid"].toString(),
            scope = token.getScope(deep = true).toSet()
        )
    }
}
