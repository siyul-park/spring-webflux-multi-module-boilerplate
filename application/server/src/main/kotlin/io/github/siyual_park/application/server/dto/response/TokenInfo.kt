package io.github.siyual_park.application.server.dto.response

import java.time.Duration

data class TokenInfo(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Duration,
    val refreshToken: String?
)
