package io.github.siyual_park.application.server.dto.response

import io.github.siyual_park.ulid.ULID
import java.time.Instant

data class UserInfo(
    val id: ULID,
    val name: String,
    val email: String,
    val scope: Collection<ScopeTokenInfo>?,
    val createdAt: Instant,
    val updatedAt: Instant?
)
