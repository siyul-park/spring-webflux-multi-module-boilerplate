package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.UserCredentialInfo
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.user.domain.UserCredential
import org.springframework.stereotype.Component

@Component
class UserCredentialMapper : Mapper<UserCredential, UserCredentialInfo> {
    override val sourceType = object : TypeReference<UserCredential>() {}
    override val targetType = object : TypeReference<UserCredentialInfo>() {}

    override suspend fun map(source: UserCredential): UserCredentialInfo {
        val raw = source.raw()
        return UserCredentialInfo(
            createdAt = raw.createdAt!!,
            updatedAt = raw.updatedAt
        )
    }
}
