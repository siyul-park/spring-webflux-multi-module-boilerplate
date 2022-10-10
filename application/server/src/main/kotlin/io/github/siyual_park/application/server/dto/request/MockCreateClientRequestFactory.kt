package io.github.siyual_park.application.server.dto.request

import io.github.siyual_park.client.entity.ClientType
import io.github.siyual_park.util.resolveNotNull
import io.github.siyual_park.util.url
import io.github.siyual_park.util.username
import net.datafaker.Faker
import java.net.URL
import java.security.SecureRandom
import java.util.Optional

object MockCreateClientRequestFactory {
    data class Template(
        val name: Optional<String>? = null,
        val type: Optional<ClientType>? = null,
        val origins: Optional<Collection<URL>>? = null
    )

    private val faker = Faker(SecureRandom())

    fun create(template: Template? = null): CreateClientRequest {
        return CreateClientRequest(
            name = resolveNotNull(template?.name) { faker.name().username(10) },
            type = resolveNotNull(template?.type) { ClientType.PUBLIC },
            origins = resolveNotNull(template?.origins) { listOf(faker.internet().url(protocol = null)) },
        )
    }
}
