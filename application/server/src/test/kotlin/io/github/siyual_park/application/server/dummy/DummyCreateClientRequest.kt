package io.github.siyual_park.application.server.dummy

import io.github.siyual_park.application.server.dto.request.CreateClientRequest
import io.github.siyual_park.client.entity.ClientType
import io.github.siyual_park.util.Presence
import java.net.URL

object DummyCreateClientRequest {
    data class Template(
        val name: Presence<String> = Presence.Empty(),
        val type: Presence<ClientType> = Presence.Empty(),
        val origin: Presence<URL> = Presence.Empty()
    )

    fun create(template: Template? = null): CreateClientRequest {
        val t = Presence.ofNullable(template)
        return CreateClientRequest(
            name = t.flatMap { it.name }.orElseGet { DummyNameFactory.create(10) },
            type = t.flatMap { it.type }.orElseGet { ClientType.PUBLIC },
            origin = t.flatMap { it.origin }.orElseGet { URL("https://localhost:8080") },
        )
    }
}
