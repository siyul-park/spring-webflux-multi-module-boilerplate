package io.github.siyual_park.application.server.dummy

import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.client.domain.CreateClientPayload
import io.github.siyual_park.client.entity.ClientType
import io.github.siyual_park.test.DummyNameFactory
import io.github.siyual_park.test.resolve
import io.github.siyual_park.test.resolveNotNull
import java.net.URL
import java.util.Optional

object DummyCreateClientPayload {
    data class Template(
        val name: Optional<String>? = null,
        val type: Optional<ClientType>? = null,
        val origin: Optional<URL>? = null,
        val scope: Optional<Collection<ScopeToken>?>? = null
    )

    fun create(template: Template? = null): CreateClientPayload {
        return CreateClientPayload(
            name = resolveNotNull(template?.name) { DummyNameFactory.create(10) },
            type = resolveNotNull(template?.type) { ClientType.CONFIDENTIAL },
            origin = resolveNotNull(template?.origin) { URL("http://localhost:8080") },
            scope = resolve(template?.scope) { null }
        )
    }
}
