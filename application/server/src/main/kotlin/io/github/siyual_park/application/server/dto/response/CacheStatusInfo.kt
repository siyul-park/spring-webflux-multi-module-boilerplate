package io.github.siyual_park.application.server.dto.response

data class CacheStatusInfo(
    val hit: Long,
    val miss: Long,
    val free: Long?,
    val used: Long?
)
