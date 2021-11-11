package io.github.siyual_park.application.external.converter.mapper

import io.github.siyual_park.application.external.dto.response.UserInfo
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.user.entity.User
import org.springframework.stereotype.Component

@Component
class UserInfoMapper : Mapper<User, UserInfo> {
    override val sourceType = object : TypeReference<User>() {}
    override val targetType = object : TypeReference<UserInfo>() {}

    override suspend fun map(source: User) = UserInfo(
        id = source.id!!,
        name = source.name,
        createdAt = source.createdAt!!,
        updatedAt = source.updatedAt,
        deletedAt = source.deletedAt
    )
}
