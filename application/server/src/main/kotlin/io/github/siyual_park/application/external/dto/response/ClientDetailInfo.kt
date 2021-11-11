package io.github.siyual_park.application.external.dto.response

import io.github.siyual_park.client.entity.ClientType
import java.time.Instant

data class ClientDetailInfo(
    val id: Long,
    val name: String,
    val type: ClientType,
    val secret: String?,
    val createdAt: Instant,
    val updatedAt: Instant?,
    val deletedAt: Instant?,
)
