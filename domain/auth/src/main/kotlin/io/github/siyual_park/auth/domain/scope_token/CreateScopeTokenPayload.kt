package io.github.siyual_park.auth.domain.scope_token

import javax.validation.constraints.Size

data class CreateScopeTokenPayload(
    @field:Size(min = 3, max = 64)
    var name: String,
    @field:Size(min = 3, max = 128)
    var description: String? = null,
    var system: Boolean = true,
)
