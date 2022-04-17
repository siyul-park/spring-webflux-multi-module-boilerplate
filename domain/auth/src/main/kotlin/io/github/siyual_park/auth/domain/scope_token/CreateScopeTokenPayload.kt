package io.github.siyual_park.auth.domain.scope_token

data class CreateScopeTokenPayload(
    var name: String,
    var description: String? = null,
    var system: Boolean = true,
)
