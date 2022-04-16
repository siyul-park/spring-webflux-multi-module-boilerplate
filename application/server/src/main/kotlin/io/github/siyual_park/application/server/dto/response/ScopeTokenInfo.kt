package io.github.siyual_park.application.server.dto.response

import io.github.siyual_park.ulid.ULID
import java.time.Instant

data class ScopeTokenInfo(
    val id: ULID,
    val name: String,
    val description: String?,
    val system: Boolean,
    val children: Collection<ScopeTokenInfo>?,
    val createdAt: Instant,
    val updatedAt: Instant?,
)
