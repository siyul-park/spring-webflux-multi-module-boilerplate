package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.UserInfo
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.user.entity.User
import org.springframework.stereotype.Component

@Component
class UserInfosMapper(
    private val userInfoMapper: UserInfoMapper
) : Mapper<Collection<User>, Collection<UserInfo>> {
    override val sourceType = object : TypeReference<Collection<User>>() {}
    override val targetType = object : TypeReference<Collection<UserInfo>>() {}

    override suspend fun map(source: Collection<User>): Collection<UserInfo> {
        return source.map { userInfoMapper.map(it) }
    }
}