package io.github.siyual_park.application.server.factory

import io.github.siyual_park.user.domain.CreateUserPayload

class CreateUserPayloadFactory {
    fun create() = CreateUserPayload(
        name = RandomNameFactory.create(10),
        password = RandomStringFactory.create(10),
    )
}
