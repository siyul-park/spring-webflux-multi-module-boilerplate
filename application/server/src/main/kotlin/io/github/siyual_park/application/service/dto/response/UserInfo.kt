package io.github.siyual_park.application.service.dto.response

import java.time.Instant

data class UserInfo(
    val id: Long,
    val name: String,
    val createdAt: Instant,
    val updatedAt: Instant?,
    val deletedAt: Instant?,
)
