package io.github.siyual_park.application.server.dummy

import io.github.siyual_park.application.server.dto.request.CreateClientRequest
import io.github.siyual_park.client.entity.ClientType
import io.github.siyual_park.test.DummyNameFactory
import io.github.siyual_park.test.resolveNotNull
import java.net.URL
import java.util.Optional

object DummyCreateClientRequest {
    data class Template(
        val name: Optional<String>? = null,
        val type: Optional<ClientType>? = null,
        val origin: Optional<URL>? = null
    )

    fun create(template: Template? = null): CreateClientRequest {
        return CreateClientRequest(
            name = resolveNotNull(template?.name) { DummyNameFactory.create(10) },
            type = resolveNotNull(template?.type) { ClientType.PUBLIC },
            origin = resolveNotNull(template?.origin) { URL("http://localhost:8080") },
        )
    }
}
