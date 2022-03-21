package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.UserInfo
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.user.domain.User
import org.springframework.stereotype.Component

@Component
class UserInfoMapper : Mapper<User, UserInfo> {
    override val sourceType = object : TypeReference<User>() {}
    override val targetType = object : TypeReference<UserInfo>() {}

    override suspend fun map(source: User) = UserInfo(
        id = source.id!!,
        name = source.name,
        createdAt = source.raw().createdAt!!,
        updatedAt = source.raw().updatedAt,
        deletedAt = source.raw().deletedAt
    )
}
