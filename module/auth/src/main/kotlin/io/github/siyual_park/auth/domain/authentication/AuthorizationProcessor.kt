package io.github.siyual_park.auth.domain.authentication

import io.github.siyual_park.auth.domain.Principal

abstract class AuthorizationProcessor<PRINCIPAL : Principal>(
    private val type: String
) : AuthenticateProcessor<AuthorizationPayload, PRINCIPAL> {
    override suspend fun authenticate(payload: AuthorizationPayload): PRINCIPAL? {
        if (payload.type.lowercase() != type.lowercase()) {
            return null
        }
        return authenticate(payload.credentials)
    }

    abstract suspend fun authenticate(credentials: String): PRINCIPAL?
}
