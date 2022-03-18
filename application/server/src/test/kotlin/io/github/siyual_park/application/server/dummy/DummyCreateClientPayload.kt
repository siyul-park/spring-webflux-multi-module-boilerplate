package io.github.siyual_park.application.server.dummy

import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.client.domain.CreateClientPayload
import io.github.siyual_park.client.entity.ClientType
import java.net.URL

object DummyCreateClientPayload {
    data class CreateClientPayloadTemplate(
        val name: Presence<String> = Presence.Empty(),
        val type: Presence<ClientType> = Presence.Empty(),
        val origin: Presence<URL> = Presence.Empty(),
        val scope: Presence<Collection<ScopeToken>?> = Presence.Empty()
    )

    fun create(template: CreateClientPayloadTemplate? = null): CreateClientPayload {
        val t = Presence.ofNullable(template)
        return CreateClientPayload(
            name = t.flatMap { it.name }.orElseGet { RandomNameFactory.create(10) },
            type = t.flatMap { it.type }.orElseGet { ClientType.PUBLIC },
            origin = t.flatMap { it.origin }.orElseGet { URL("https://localhost:8080") },
            scope = t.flatMap { it.scope }.orElseGet { null }
        )
    }
}
