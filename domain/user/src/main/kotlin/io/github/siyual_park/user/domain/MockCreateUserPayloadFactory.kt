package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.util.resolve
import io.github.siyual_park.util.resolveNotNull
import io.github.siyual_park.util.username
import net.datafaker.Faker
import java.security.SecureRandom
import java.util.Optional

object MockCreateUserPayloadFactory {
    data class Template(
        val name: Optional<String>? = null,
        val email: Optional<String>? = null,
        val password: Optional<String>? = null,
        val scope: Optional<Collection<ScopeToken>>? = null
    )

    private val faker = Faker(SecureRandom())

    fun create(template: Template? = null): CreateUserPayload {
        return CreateUserPayload(
            name = resolveNotNull(template?.name) { faker.name().username(16) },
            email = resolveNotNull(template?.email) { faker.internet().emailAddress() },
            password = resolveNotNull(template?.password) { faker.internet().password() },
            scope = resolve(template?.scope) { null }
        )
    }
}
