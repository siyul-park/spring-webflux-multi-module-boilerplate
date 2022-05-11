package io.github.siyual_park.auth.domain.authentication

import io.github.siyual_park.auth.domain.Principal

abstract class AuthorizationStrategy<PRINCIPAL : Principal>(
    private val type: String
) : AuthenticateStrategy<AuthorizationPayload, PRINCIPAL> {
    override val clazz = AuthorizationPayload::class

    override suspend fun authenticate(payload: AuthorizationPayload): PRINCIPAL? {
        if (payload.type.lowercase() != type.lowercase()) {
            return null
        }
        return authenticate(payload.credentials)
    }

    abstract suspend fun authenticate(credentials: String): PRINCIPAL?
}
