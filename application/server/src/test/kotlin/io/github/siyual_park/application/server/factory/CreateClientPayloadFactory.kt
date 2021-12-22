package io.github.siyual_park.application.server.factory

import io.github.siyual_park.client.domain.CreateClientPayload
import io.github.siyual_park.client.entity.ClientType
import java.net.URL

class CreateClientPayloadFactory {
    fun create() = CreateClientPayload(
        name = RandomNameFactory.create(10),
        type = ClientType.PUBLIC,
        origin = URL("https://localhost:8080")
    )
}
