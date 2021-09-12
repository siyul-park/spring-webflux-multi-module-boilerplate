package io.github.siyual_park.auth.domain.principal

import io.github.siyual_park.auth.entity.ScopeToken

data class UserPrincipal(
    override val id: String,
    override val scope: Set<ScopeToken>
) : Principal
