package io.github.siyual_park.auth.domain.token

data class TokenContainer(
    val accessToken: Token,
    val refreshToken: Token?
)
