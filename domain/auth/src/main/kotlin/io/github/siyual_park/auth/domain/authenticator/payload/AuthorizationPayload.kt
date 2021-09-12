package io.github.siyual_park.auth.domain.authenticator.payload

data class AuthorizationPayload(
    val type: String,
    val credentials: String,
) : AuthenticationPayload
