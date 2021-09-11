package io.github.siyual_park.auth.domain.authenticator

import io.github.siyual_park.auth.entity.ScopeToken

data class UserAuthentication(
    override val id: Long,
    override val scope: Set<ScopeToken>
) : Authentication<Long>
