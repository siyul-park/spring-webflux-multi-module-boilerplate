package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.client.entity.ClientType
import io.github.siyual_park.ulid.ULID
import java.net.URL
import javax.validation.constraints.Size

data class CreateClientPayload(
    @field:Size(min = 3, max = 20)
    val name: String,
    val type: ClientType,
    @field:Size(max = 2048)
    val origin: URL,
    val scope: Collection<ScopeToken>? = null,
    val id: ULID? = null
)
