package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeToken

data class CreateUserPayload(
    val name: String,
    val email: String,
    val password: String,
    val scope: Collection<ScopeToken>? = null
)
