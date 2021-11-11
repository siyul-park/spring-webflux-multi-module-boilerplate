package io.github.siyual_park.client.domain.auth

data class ClientCredentialsGrantPayload(
    val id: Long,
    val secret: String?,
)
