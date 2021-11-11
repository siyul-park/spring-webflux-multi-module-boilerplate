package io.github.siyual_park.application.service.factory

import io.github.siyual_park.client.domain.CreateClientPayload
import io.github.siyual_park.client.entity.ClientType

class CreateClientPayloadFactory {
    fun create() = CreateClientPayload(
        name = RandomNameFactory.create(10),
        type = ClientType.PUBLIC
    )
}
