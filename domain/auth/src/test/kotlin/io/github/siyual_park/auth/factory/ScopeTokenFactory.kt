package io.github.siyual_park.auth.factory

import io.github.siyual_park.auth.entity.ScopeToken
import java.util.UUID

class ScopeTokenFactory {
    fun create(default: Boolean) = ScopeToken(
        name = UUID.randomUUID().toString().slice(0..10),
        system = true,
        default = default
    )
}
