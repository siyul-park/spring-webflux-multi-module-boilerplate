package io.github.siyual_park.application.external.converter.mapper

import io.github.siyual_park.application.external.dto.response.CreateUserResponse
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.user.entity.User
import org.springframework.stereotype.Component

@Component
class CreateUserResponseMapper : Mapper<User, CreateUserResponse> {
    override val sourceClazz = User::class
    override val targetClazz = CreateUserResponse::class

    override suspend fun map(source: User) = CreateUserResponse(
        id = source.id!!,
        name = source.name,
        createdAt = source.createdAt!!,
        updatedAt = source.updatedAt!!
    )
}
