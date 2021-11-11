package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.client.entity.ClientEntity

data class PasswordGrantPayload(
    val username: String,
    val password: String,
    val client: ClientEntity
)
