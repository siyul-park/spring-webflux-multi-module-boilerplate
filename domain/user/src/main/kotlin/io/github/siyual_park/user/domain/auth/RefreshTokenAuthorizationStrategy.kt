package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.authentication.AuthenticateMapping
import io.github.siyual_park.auth.domain.authentication.AuthenticateStrategy
import io.github.siyual_park.auth.domain.authentication.RefreshTokenPayload
import io.github.siyual_park.auth.domain.token.TokenStorage
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.flow.toSet
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@AuthenticateMapping
@Order(Ordered.LOWEST_PRECEDENCE - 1)
class RefreshTokenAuthorizationStrategy(
    private val tokenStorage: TokenStorage
) : AuthenticateStrategy<RefreshTokenPayload, UserPrincipal> {
    override val clazz = RefreshTokenPayload::class

    override suspend fun authenticate(payload: RefreshTokenPayload): UserPrincipal? {
        val token = tokenStorage.loadOrFail(payload.refreshToken)
        if (token["uid"] == null) {
            return null
        }

        return UserPrincipal(
            id = token["uid"].toString(),
            clientId = token["cid"]?.toString()?.let { ULID.fromString(it) },
            scope = token.getScope(deep = true).toSet()
        )
    }
}
