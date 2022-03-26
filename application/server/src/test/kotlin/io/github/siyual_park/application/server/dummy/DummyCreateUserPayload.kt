package io.github.siyual_park.application.server.dummy

import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.user.domain.CreateUserPayload
import io.github.siyual_park.util.Presence

object DummyCreateUserPayload {
    data class Template(
        val name: Presence<String> = Presence.Empty(),
        val email: Presence<String> = Presence.Empty(),
        val password: Presence<String> = Presence.Empty(),
        val scope: Presence<Collection<ScopeToken>?> = Presence.Empty()
    )

    fun create(template: Template? = null): CreateUserPayload {
        val t = Presence.ofNullable(template)
        return CreateUserPayload(
            name = t.flatMap { it.name }.orElseGet { DummyNameFactory.create(10) },
            email = t.flatMap { it.email }.orElseGet { DummyEmailFactory.create(15) },
            password = t.flatMap { it.password }.orElseGet { DummyStringFactory.create(10) },
            scope = t.flatMap { it.scope }.orElseGet { null }
        )
    }
}
