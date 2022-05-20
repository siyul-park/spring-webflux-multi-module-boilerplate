package io.github.siyual_park.auth.dummy

import io.github.siyual_park.auth.domain.scope_token.CreateScopeTokenPayload
import io.github.siyual_park.test.DummyStringFactory
import io.github.siyual_park.test.resolveNotNull
import java.util.Optional

object DummyCreateScopeTokenPayload {
    data class Template(
        val name: Optional<String>? = null,
        val description: Optional<String?>? = null,
    )

    fun create(template: Template? = null): CreateScopeTokenPayload {
        return CreateScopeTokenPayload(
            name = resolveNotNull(template?.name) { DummyScopeNameFactory.create(10) },
            description = resolveNotNull(template?.name) { DummyStringFactory.create(10) },
        )
    }
}
