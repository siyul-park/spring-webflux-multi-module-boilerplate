package io.github.siyual_park.application.external.converter.mapper

import io.github.siyual_park.application.external.dto.request.CreateUserRequest
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.user.domain.CreateUserPayload
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
