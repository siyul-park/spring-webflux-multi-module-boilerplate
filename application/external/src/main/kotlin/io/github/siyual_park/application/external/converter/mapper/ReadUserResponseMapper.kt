package io.github.siyual_park.application.external.converter.mapper

import io.github.siyual_park.application.external.dto.response.ReadUserResponse
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.user.entity.User
import org.springframework.stereotype.Component

@Component
class ReadUserResponseMapper : Mapper<User, ReadUserResponse> {
    override val sourceType = object : TypeReference<User>() {}
    override val targetType = object : TypeReference<ReadUserResponse>() {}

    override suspend fun map(source: User) = ReadUserResponse(
        id = source.id!!,
        name = source.name,
        createdAt = source.createdAt!!,
        updatedAt = source.updatedAt!!
    )
}
