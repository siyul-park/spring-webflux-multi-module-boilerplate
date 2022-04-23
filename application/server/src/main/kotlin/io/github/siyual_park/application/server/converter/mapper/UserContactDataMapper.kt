package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.UserContactInfo
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.user.entity.UserContactData
import org.springframework.stereotype.Component

@Component
class UserContactDataMapper : Mapper<UserContactData, UserContactInfo> {
    override val sourceType = object : TypeReference<UserContactData>() {}
    override val targetType = object : TypeReference<UserContactInfo>() {}

    override suspend fun map(source: UserContactData): UserContactInfo {
        return UserContactInfo(
            email = source.email,
            updatedAt = source.updatedAt
        )
    }
}
