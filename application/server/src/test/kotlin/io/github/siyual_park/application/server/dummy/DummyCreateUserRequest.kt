package io.github.siyual_park.application.server.dummy

import io.github.siyual_park.application.server.dto.request.CreateUserRequest
import io.github.siyual_park.util.Presence

object DummyCreateUserRequest {
    data class CreateUserRequestTemplate(
        val name: Presence<String> = Presence.Empty(),
        val password: Presence<String> = Presence.Empty(),
    )

    fun create(template: CreateUserRequestTemplate? = null): CreateUserRequest {
        val t = Presence.ofNullable(template)
        return CreateUserRequest(
            name = t.flatMap { it.name }.orElseGet { RandomNameFactory.create(10) },
            password = t.flatMap { it.password }.orElseGet { RandomNameFactory.create(10) },
        )
    }
}
