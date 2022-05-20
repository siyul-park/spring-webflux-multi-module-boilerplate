package io.github.siyual_park.application.server.dummy

import io.github.siyual_park.application.server.dto.request.CreateUserRequest
import io.github.siyual_park.test.DummyEmailFactory
import io.github.siyual_park.test.DummyNameFactory
import io.github.siyual_park.test.DummyStringFactory
import io.github.siyual_park.test.resolveNotNull
import java.util.Optional

object DummyCreateUserRequest {
    data class Template(
        val name: Optional<String>? = null,
        val email: Optional<String>? = null,
        val password: Optional<String>? = null,
    )

    fun create(template: Template? = null): CreateUserRequest {
        return CreateUserRequest(
            name = resolveNotNull(template?.name) { DummyNameFactory.create(10) },
            email = resolveNotNull(template?.email) { DummyEmailFactory.create(15) },
            password = resolveNotNull(template?.password) { DummyStringFactory.create(10) },
        )
    }
}
