package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.authentication.AuthenticateMapping
import io.github.siyual_park.auth.domain.authentication.AuthorizationPayload
import io.github.siyual_park.auth.domain.authentication.AuthorizationStrategy
import io.github.siyual_park.auth.domain.token.TokenStorage
import io.github.siyual_park.persistence.loadOrFail
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.flow.toSet
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@AuthenticateMapping(filterBy = AuthorizationPayload::class)
@Order(Ordered.LOWEST_PRECEDENCE - 1)
class BearerAuthorizationStrategy(
    private val tokenStorage: TokenStorage
) : AuthorizationStrategy<UserPrincipal>("Bearer") {
    override suspend fun authenticate(credentials: String): UserPrincipal? {
        val token = tokenStorage.loadOrFail(credentials)
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
