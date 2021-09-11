package io.github.siyual_park.auth.domain.authenticator

data class PasswordGrantInfo(
    val username: String,
    val password: String,
) : AuthenticationInfo
