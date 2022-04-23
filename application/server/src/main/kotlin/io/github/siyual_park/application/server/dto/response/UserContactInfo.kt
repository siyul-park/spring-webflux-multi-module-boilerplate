package io.github.siyual_park.application.server.dto.response

import java.time.Instant

data class UserContactInfo(
    val email: String,
    val createdAt: Instant,
    val updatedAt: Instant?
)
