package io.github.siyual_park.auth.domain

data class CreateUserPayload(
    val name: String,
    val password: String,
)
