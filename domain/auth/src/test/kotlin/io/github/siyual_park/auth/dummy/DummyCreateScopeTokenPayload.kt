package io.github.siyual_park.auth.dummy

import io.github.siyual_park.auth.domain.scope_token.CreateScopeTokenPayload
import io.github.siyual_park.util.Presence

object DummyCreateScopeTokenPayload {
    data class Template(
        val name: Presence<String> = Presence.Empty(),
        val description: Presence<String?> = Presence.Empty(),
    )

    fun create(template: Template? = null): CreateScopeTokenPayload {
        val t = Presence.ofNullable(template)
        return CreateScopeTokenPayload(
            name = t.flatMap { it.name }.orElseGet { DummyScopeNameFactory.create(10) },
            description = t.flatMap { it.description }.orElseGet { DummyStringFactory.create(10) },
        )
    }
}
