package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.entity.ScopeToken

data class UserPrincipal(
    override val id: String,
    override val scope: Set<ScopeToken>
) : Principal
