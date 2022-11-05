package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.client.entity.ClientAssociable
import io.github.siyual_park.ulid.ULID
import io.github.siyual_park.user.entity.UserAssociable

data class UserPrincipal(
    override val id: ULID = ULID.randomULID(),
    override val userId: ULID,
    override val clientId: ULID? = null,
    override var scope: Set<ScopeToken>,
) : Principal, UserAssociable, ClientAssociable
