package io.github.siyual_park.auth.domain

data class UserAuthenticationInformation(
    val username: String,
    val password: String
) : AuthenticationInformation
