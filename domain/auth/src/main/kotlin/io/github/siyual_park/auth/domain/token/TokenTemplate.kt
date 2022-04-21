package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.scope_token.ScopeToken

data class TokenTemplate(
    val type: String,
    val limit: List<Pair<String, Int>>? = null,
    val pop: Set<ScopeToken>? = null,
    val push: Set<ScopeToken>? = null,
    val filter: Set<ScopeToken>? = null,
)
