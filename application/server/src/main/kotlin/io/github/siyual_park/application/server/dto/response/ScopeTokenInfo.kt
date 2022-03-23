package io.github.siyual_park.application.server.dto.response

import java.time.Instant

data class ScopeTokenInfo(
    val id: Long,
    val name: String,
    val description: String?,
    val system: Boolean,
    val children: Collection<ScopeTokenInfo>?,
    val createdAt: Instant,
    val updatedAt: Instant?,
)
