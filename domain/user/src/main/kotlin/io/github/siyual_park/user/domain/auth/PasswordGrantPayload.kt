package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.ulid.ULID

data class PasswordGrantPayload(
    val username: String,
    val password: String,
    val clientId: ULID?
)
