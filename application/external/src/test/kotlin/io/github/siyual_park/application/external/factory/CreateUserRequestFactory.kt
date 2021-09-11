package io.github.siyual_park.application.external.factory

import io.github.siyual_park.application.external.dto.request.CreateUserRequest
import java.util.UUID

class CreateUserRequestFactory {
    fun create() = CreateUserRequest(
        name = UUID.randomUUID().toString().slice(0..10),
        password = UUID.randomUUID().toString().slice(0..10),
    )
}
