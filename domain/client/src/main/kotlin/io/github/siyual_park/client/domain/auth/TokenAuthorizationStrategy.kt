package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.authentication.AuthenticateStrategy
import io.github.siyual_park.auth.domain.token.TokenStorage
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.flow.toSet

class TokenAuthorizationStrategy(
    private val tokenStorage: TokenStorage
) : AuthenticateStrategy<String, ClientPrincipal> {
    override val clazz = String::class

    override suspend fun authenticate(payload: String): ClientPrincipal? {
        val token = tokenStorage.loadOrFail(payload)
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
