package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.util.resolveNotNull
import net.datafaker.Faker
import java.util.Optional

object MockCreateScopeTokenPayloadFactory {
    private val faker = Faker()

    data class Template(
        val name: Optional<String>? = null,
        val description: Optional<String?>? = null,
    )

    fun create(template: Template? = null): CreateScopeTokenPayload {
        return CreateScopeTokenPayload(
            name = resolveNotNull(template?.name) { MockScopeNameFactory.create(10) },
            description = resolveNotNull(template?.name) { faker.lorem().sentence() },
        )
    }
}
