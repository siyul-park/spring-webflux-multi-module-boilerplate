package io.github.siyual_park.auth.domain.authenticator

interface AuthorizationAuthenticateProcessor<PRINCIPAL : Principal> {
    val type: String

    suspend fun authenticate(credentials: String): PRINCIPAL
}
