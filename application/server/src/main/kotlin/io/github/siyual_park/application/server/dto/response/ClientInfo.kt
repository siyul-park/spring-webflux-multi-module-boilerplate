package io.github.siyual_park.application.server.dto.response

import io.github.siyual_park.client.entity.ClientType
import java.time.Instant

data class ClientInfo(
    val id: Long,
    val name: String,
    val type: ClientType,
    val createdAt: Instant,
    val updatedAt: Instant?,
    val deletedAt: Instant?,
)
