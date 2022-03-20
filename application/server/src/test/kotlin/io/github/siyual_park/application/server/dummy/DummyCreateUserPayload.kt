package io.github.siyual_park.application.server.dummy

import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.user.domain.CreateUserPayload
import io.github.siyual_park.util.Presence

object DummyCreateUserPayload {
    data class CreateUserPayloadTemplate(
        val name: Presence<String> = Presence.Empty(),
        val password: Presence<String> = Presence.Empty(),
        val scope: Presence<Collection<ScopeToken>?> = Presence.Empty()
    )

    fun create(template: CreateUserPayloadTemplate? = null): CreateUserPayload {
        val t = Presence.ofNullable(template)
        return CreateUserPayload(
            name = t.flatMap { it.name }.orElseGet { RandomNameFactory.create(10) },
            password = t.flatMap { it.password }.orElseGet { RandomNameFactory.create(10) },
            scope = t.flatMap { it.scope }.orElseGet { null }
        )
    }
}
