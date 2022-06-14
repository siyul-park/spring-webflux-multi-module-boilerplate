package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.client.entity.ClientEntity
import io.github.siyual_park.ulid.ULID

data class ClientPrincipal(
    override val id: ULID = ULID.randomULID(),
    override val clientId: ULID,
    override var scope: Set<ScopeToken>,
) : Principal, ClientEntity
