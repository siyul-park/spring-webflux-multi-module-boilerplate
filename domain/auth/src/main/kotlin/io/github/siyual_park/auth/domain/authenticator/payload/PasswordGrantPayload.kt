package io.github.siyual_park.auth.domain.authenticator.payload

data class PasswordGrantPayload(
    val username: String,
    val password: String,
) : AuthenticationPayload
