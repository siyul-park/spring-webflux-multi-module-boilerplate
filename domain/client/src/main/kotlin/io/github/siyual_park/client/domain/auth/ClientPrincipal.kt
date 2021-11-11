package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.client.entity.ClientEntity

data class ClientPrincipal(
    override val id: String,
    override val scope: Set<ScopeToken>
) : Principal, ClientEntity {
    override val clientId: Long
        get() = id.toLong()
}
