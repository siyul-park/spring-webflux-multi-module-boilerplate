package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.ulid.ULID

data class ClientCredentialsGrantPayload(
    val id: ULID,
    val secret: String?,
)
