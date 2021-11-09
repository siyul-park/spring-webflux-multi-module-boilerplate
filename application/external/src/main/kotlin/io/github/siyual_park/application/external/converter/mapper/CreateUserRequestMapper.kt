package io.github.siyual_park.application.external.converter.mapper

import io.github.siyual_park.application.external.dto.request.CreateUserRequest
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.user.domain.CreateUserPayload
import org.springframework.stereotype.Component

@Component
class CreateUserRequestMapper : Mapper<CreateUserRequest, CreateUserPayload> {
    override val sourceType = object : TypeReference<CreateUserRequest>() {}
    override val targetType = object : TypeReference<CreateUserPayload>() {}

    override suspend fun map(source: CreateUserRequest) = CreateUserPayload(
        username = source.name,
        password = source.password
    )
}
