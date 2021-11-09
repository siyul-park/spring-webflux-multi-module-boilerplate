package io.github.siyual_park.error.domain

data class ErrorInfo(
    val path: String,
    val error: String,
    val description: String?
)
