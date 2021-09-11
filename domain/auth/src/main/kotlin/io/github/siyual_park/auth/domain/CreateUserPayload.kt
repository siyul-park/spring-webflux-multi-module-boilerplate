package io.github.siyual_park.auth.domain

data class CreateUserPayload(
    val username: String,
    val password: String,
)
