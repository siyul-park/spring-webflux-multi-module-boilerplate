package io.github.siyual_park.application.server.dto.response

import java.time.Instant

data class UserCredentialInfo(
    val createdAt: Instant,
    val updatedAt: Instant?
)
