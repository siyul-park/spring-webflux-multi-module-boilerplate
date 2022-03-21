package io.github.siyual_park.application.server.dto.response

data class ErrorInfo(
    val path: String,
    val error: String,
    val description: String?
)
