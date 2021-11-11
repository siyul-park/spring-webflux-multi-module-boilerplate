package io.github.siyual_park.user.domain.auth

data class PasswordGrantPayload(
    val username: String,
    val password: String,
    val clientId: Long?
)
