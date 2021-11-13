package io.github.siyual_park.application.server.factory

import io.github.siyual_park.application.server.dto.request.CreateUserRequest

class CreateUserRequestFactory {
    fun create() = CreateUserRequest(
        name = RandomNameFactory.create(10),
        password = RandomStringFactory.create(10),
    )
}
