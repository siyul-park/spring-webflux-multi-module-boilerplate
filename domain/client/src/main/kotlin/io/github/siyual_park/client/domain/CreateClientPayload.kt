package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.client.entity.ClientType
import java.net.URL

data class CreateClientPayload(
    val name: String,
    val type: ClientType,
    val origin: URL,
    val scope: Collection<ScopeToken>? = null
)
