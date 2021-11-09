package io.github.siyual_park.auth.domain.authenticator

data class AuthorizationPayload(
    val type: String,
    val credentials: String,
)
