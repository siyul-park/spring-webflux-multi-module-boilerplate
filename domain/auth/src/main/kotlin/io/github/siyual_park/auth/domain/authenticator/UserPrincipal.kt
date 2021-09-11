package io.github.siyual_park.auth.domain.authenticator

import io.github.siyual_park.auth.entity.ScopeToken

data class UserPrincipal(
    override val id: Long,
    override val scope: Set<ScopeToken>
) : Principal<Long>
