package io.github.siyual_park.application.server.dummy

import io.github.siyual_park.application.server.dto.request.CreateScopeTokenRequest
import io.github.siyual_park.util.Presence

object DummyCreateScopeTokenRequest {
    data class Template(
        val name: Presence<String> = Presence.Empty(),
        val description: Presence<String?> = Presence.Empty(),
    )

    fun create(template: Template? = null): CreateScopeTokenRequest {
        val t = Presence.ofNullable(template)
        return CreateScopeTokenRequest(
            name = t.flatMap { it.name }.orElseGet { DummyScopeNameFactory.create(10) },
            description = t.flatMap { it.description }.orElseGet { DummyStringFactory.create(10) },
        )
    }
}
