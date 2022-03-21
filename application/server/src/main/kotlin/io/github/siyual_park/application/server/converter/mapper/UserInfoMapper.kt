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

    override suspend fun map(source: User): UserInfo {
        val raw = source.raw()
        return UserInfo(
            id = raw.id!!,
            name = raw.name,
            createdAt = raw.createdAt!!,
            updatedAt = raw.updatedAt,
            deletedAt = raw.deletedAt
        )
    }
}
