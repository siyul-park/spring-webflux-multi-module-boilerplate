package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import javax.validation.constraints.Email
import javax.validation.constraints.Size

data class CreateUserPayload(
    @field:Size(min = 3, max = 64)
    val name: String,
    @field:Email
    @field:Size(min = 8, max = 128)
    val email: String,
    val password: String,
    @field:Size(min = 8, max = 20)
    val scope: Collection<ScopeToken>? = null
)
