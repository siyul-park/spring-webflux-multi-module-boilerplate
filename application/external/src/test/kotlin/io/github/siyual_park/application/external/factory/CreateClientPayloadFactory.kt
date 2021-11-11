package io.github.siyual_park.application.external.factory

import io.github.siyual_park.client.domain.CreateClientPayload
import io.github.siyual_park.client.entity.ClientType

class CreateClientPayloadFactory {
    fun create() = CreateClientPayload(
        name = RandomUsernameFactory.create(10),
        type = ClientType.PUBLIC
    )
}
