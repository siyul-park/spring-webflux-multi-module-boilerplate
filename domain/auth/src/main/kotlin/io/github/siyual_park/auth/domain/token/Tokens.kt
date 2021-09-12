package io.github.siyual_park.auth.domain.token

import java.time.Duration

data class Tokens(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Duration,
    val refreshToken: String?
)
