package io.github.siyual_park.application.server.dummy

import io.github.siyual_park.application.server.dto.request.CreateUserRequest
import io.github.siyual_park.util.Presence

object DummyCreateUserRequest {
    data class Template(
        val name: Presence<String> = Presence.Empty(),
        val email: Presence<String> = Presence.Empty(),
        val password: Presence<String> = Presence.Empty(),
    )

    fun create(template: Template? = null): CreateUserRequest {
        val t = Presence.ofNullable(template)
        return CreateUserRequest(
            name = t.flatMap { it.name }.orElseGet { DummyNameFactory.create(10) },
            email = t.flatMap { it.email }.orElseGet { DummyEmailFactory.create(15) },
            password = t.flatMap { it.password }.orElseGet { DummyNameFactory.create(10) },
        )
    }
}
