package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.client.entity.ClientType
import io.github.siyual_park.util.resolve
import io.github.siyual_park.util.resolveNotNull
import io.github.siyual_park.util.url
import io.github.siyual_park.util.username
import net.datafaker.Faker
import java.net.URL
import java.security.SecureRandom
import java.util.Optional

object MockCreateClientPayloadFactory {
    data class Template(
        val name: Optional<String>? = null,
        val type: Optional<ClientType>? = null,
        val origin: Optional<URL>? = null,
        val scope: Optional<Collection<ScopeToken>>? = null
    )

    private val faker = Faker(SecureRandom())

    fun create(template: Template? = null): CreateClientPayload {
        return CreateClientPayload(
            name = resolveNotNull(template?.name) { faker.name().username(10) },
            type = resolveNotNull(template?.type) { ClientType.CONFIDENTIAL },
            origin = resolveNotNull(template?.origin) { faker.internet().url(protocol = null) },
            scope = resolve(template?.scope) { null }
        )
    }
}
