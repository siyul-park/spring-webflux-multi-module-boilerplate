package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.client.entity.ClientEntity
import io.github.siyual_park.ulid.ULID
import io.github.siyual_park.user.entity.UserEntity

data class UserPrincipal(
    override val id: ULID = ULID.randomULID(),
    override val userId: ULID,
    override val clientId: ULID? = null,
    override var scope: Set<ScopeToken>,
) : Principal, UserEntity, ClientEntity
