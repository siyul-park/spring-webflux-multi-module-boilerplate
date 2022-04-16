package io.github.siyual_park.auth.domain.token

import java.time.Duration

data class Token(
    val value: String,
    val type: String,
    val expiresIn: Duration
)
