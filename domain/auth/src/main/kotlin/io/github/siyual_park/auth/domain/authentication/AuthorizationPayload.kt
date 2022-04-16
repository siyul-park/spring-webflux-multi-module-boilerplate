package io.github.siyual_park.auth.domain.authentication

data class AuthorizationPayload(
    val type: String,
    val credentials: String,
)
