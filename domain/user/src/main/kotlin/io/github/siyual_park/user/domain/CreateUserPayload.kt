package io.github.siyual_park.user.domain

data class CreateUserPayload(
    val name: String,
    val password: String,
)
