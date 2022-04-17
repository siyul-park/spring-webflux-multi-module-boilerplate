package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.authentication.AuthenticateMapping
import io.github.siyual_park.auth.domain.authentication.AuthorizationPayload
import io.github.siyual_park.auth.domain.authentication.AuthorizationStrategy
import io.github.siyual_park.auth.domain.token.TokenStorage
import io.github.siyual_park.persistence.loadOrFail
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.flow.toSet
import org.springframework.stereotype.Component

@Component
@AuthenticateMapping(filterBy = AuthorizationPayload::class)
class BearerAuthorizationStrategy(
    private val tokenStorage: TokenStorage
) : AuthorizationStrategy<UserPrincipal>("bearer") {
    override suspend fun authenticate(credentials: String): UserPrincipal? {
        val token = tokenStorage.loadOrFail(credentials)
        val claims = token.claims
        if (claims["uid"] == null) {
            return null
        }

        return UserPrincipal(
            id = claims["uid"].toString(),
            clientId = claims["cid"]?.toString()?.let { ULID.fromString(it) },
            scope = token.getScope(deep = true).toSet()
        )
    }
}
