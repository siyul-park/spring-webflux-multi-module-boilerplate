package io.github.siyual_park.application.external.factory

import io.github.siyual_park.user.domain.CreateUserPayload
import java.util.UUID

class CreateUserPayloadFactory {
    fun create() = CreateUserPayload(
        username = UUID.randomUUID().toString().slice(0..10),
        password = UUID.randomUUID().toString().slice(0..10),
    )
}
