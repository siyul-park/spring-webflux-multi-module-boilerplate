package io.github.siyual_park.application.external.dto.response

import java.time.Instant

data class ReadUserResponse(
    val id: Long,
    val name: String,
    val createdAt: Instant,
    val updatedAt: Instant?
)
