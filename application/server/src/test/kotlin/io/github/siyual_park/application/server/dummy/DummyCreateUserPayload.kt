package io.github.siyual_park.application.server.dummy

import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.test.DummyEmailFactory
import io.github.siyual_park.test.DummyNameFactory
import io.github.siyual_park.test.DummyStringFactory
import io.github.siyual_park.test.resolve
import io.github.siyual_park.test.resolveNotNull
import io.github.siyual_park.user.domain.CreateUserPayload
import java.util.Optional

object DummyCreateUserPayload {
    data class Template(
        val name: Optional<String>? = null,
        val email: Optional<String>? = null,
        val password: Optional<String>? = null,
        val scope: Optional<Collection<ScopeToken>?>? = null
    )

    fun create(template: Template? = null): CreateUserPayload {
        return CreateUserPayload(
            name = resolveNotNull(template?.name) { DummyNameFactory.create(10) },
            email = resolveNotNull(template?.email) { DummyEmailFactory.create(15) },
            password = resolveNotNull(template?.password) { DummyStringFactory.create(10) },
            scope = resolve(template?.scope) { null }
        )
    }
}
