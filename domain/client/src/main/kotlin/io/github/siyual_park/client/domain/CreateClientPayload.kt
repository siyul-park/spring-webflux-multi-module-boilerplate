package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.client.entity.ClientType
import io.github.siyual_park.ulid.ULID
import java.net.URL

data class CreateClientPayload(
    val name: String,
    val type: ClientType,
    val origin: URL,
    val scope: Collection<ScopeToken>? = null,
    val id: ULID? = null
)
