package io.github.siyual_park.application.server.dummy

import io.github.siyual_park.application.server.dto.request.CreateScopeTokenRequest
import io.github.siyual_park.test.DummyStringFactory
import io.github.siyual_park.test.resolve
import io.github.siyual_park.test.resolveNotNull
import java.util.Optional

object DummyCreateScopeTokenRequest {
    data class Template(
        val name: Optional<String>? = null,
        val description: Optional<String>? = null,
    )

    fun create(template: Template? = null): CreateScopeTokenRequest {
        return CreateScopeTokenRequest(
            name = resolveNotNull(template?.name) { DummyScopeNameFactory.create(10) },
            description = resolve(template?.name) { DummyStringFactory.create(10) },
        )
    }
}
