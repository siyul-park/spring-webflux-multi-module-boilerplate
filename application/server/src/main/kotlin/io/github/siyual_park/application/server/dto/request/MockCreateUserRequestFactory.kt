package io.github.siyual_park.application.server.dto.request

import io.github.siyual_park.util.resolveNotNull
import io.github.siyual_park.util.username
import net.datafaker.Faker
import java.security.SecureRandom
import java.util.Optional

object MockCreateUserRequestFactory {
    data class Template(
        val name: Optional<String>? = null,
        val email: Optional<String>? = null,
        val password: Optional<String>? = null,
    )

    private val faker = Faker(SecureRandom())

    fun create(template: Template? = null): CreateUserRequest {
        return CreateUserRequest(
            name = resolveNotNull(template?.name) { faker.name().username(16) },
            email = resolveNotNull(template?.email) { faker.internet().emailAddress() },
            password = resolveNotNull(template?.password) { faker.internet().password() },
        )
    }
}
