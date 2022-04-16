package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.client.entity.ClientEntity
import io.github.siyual_park.ulid.ULID

data class ClientPrincipal(
    override val id: String,
    override var scope: Set<ScopeToken>
) : Principal, ClientEntity {
    override val clientId: ULID by lazy { ULID.fromString(id) }
}
