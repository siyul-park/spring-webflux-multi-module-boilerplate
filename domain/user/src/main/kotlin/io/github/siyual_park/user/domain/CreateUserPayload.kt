package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.entity.ScopeToken

data class CreateUserPayload(
    val name: String,
    val password: String,
    val scope: Collection<ScopeToken>? = null
)
