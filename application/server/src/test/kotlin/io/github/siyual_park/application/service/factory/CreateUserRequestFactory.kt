package io.github.siyual_park.application.service.factory

import io.github.siyual_park.application.service.dto.request.CreateUserRequest

class CreateUserRequestFactory {
    fun create() = CreateUserRequest(
        name = RandomNameFactory.create(10),
        password = RandomStringFactory.create(10),
    )
}
