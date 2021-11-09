package io.github.siyual_park.application.external.dto.response

data class TokenInfo(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val refreshToken: String?
)
