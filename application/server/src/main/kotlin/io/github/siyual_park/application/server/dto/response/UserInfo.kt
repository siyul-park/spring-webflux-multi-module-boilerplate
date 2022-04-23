package io.github.siyual_park.application.server.dto.response

import io.github.siyual_park.ulid.ULID
import java.time.Instant

data class UserInfo(
    val id: ULID,
    val name: String,
    val contact: UserContactInfo?,
    val createdAt: Instant,
    val updatedAt: Instant?
)
