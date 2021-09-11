package io.github.siyual_park.application.external.mapper

import io.github.siyual_park.application.external.dto.request.CreateUserRequest
import io.github.siyual_park.auth.domain.CreateUserPayload
import io.github.siyual_park.mapper.Mapper
import org.springframework.stereotype.Component

@Component
class CreateUserRequestMapper : Mapper<CreateUserRequest, CreateUserPayload> {
    override val sourceClazz = CreateUserRequest::class
    override val targetClazz = CreateUserPayload::class

    override suspend fun map(source: CreateUserRequest) = CreateUserPayload(
        username = source.name,
        password = source.password
    )
}
