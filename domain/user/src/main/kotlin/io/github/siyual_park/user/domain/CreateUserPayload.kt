package io.github.siyual_park.user.domain

data class CreateUserPayload(
    val username: String,
    val password: String,
)
