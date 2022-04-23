package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.UserContactInfo
import io.github.siyual_park.application.server.dto.response.UserInfo
import io.github.siyual_park.auth.domain.PrincipalProvider
import io.github.siyual_park.auth.domain.authorization.Authorizator
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.mapper.map
import io.github.siyual_park.persistence.AsyncLazy
import io.github.siyual_park.user.domain.User
import org.springframework.stereotype.Component

@Component
class UserInfoMapper(
    private val mapperContext: MapperContext,
    private val principalProvider: PrincipalProvider,
    private val authorizator: Authorizator,
    private val scopeTokenStorage: ScopeTokenStorage
) : Mapper<User, UserInfo> {
    override val sourceType = object : TypeReference<User>() {}
    override val targetType = object : TypeReference<UserInfo>() {}

    private val contactSelfReadScopeToken = AsyncLazy {
        scopeTokenStorage.loadOrFail("users[self].contact:read")
    }
    private val contactReadScopeToken = AsyncLazy {
        scopeTokenStorage.loadOrFail("users.contact:read")
    }

    override suspend fun map(source: User): UserInfo {
        val principal = principalProvider.get()
        val contact: UserContactInfo? = if (
            principal != null &&
            authorizator.authorize(
                principal,
                listOf(contactSelfReadScopeToken.get(), contactReadScopeToken.get()),
                listOf(source.id, null)
            )
        ) {
            mapperContext.map(source.getContact())
        } else {
            null
        }

        val raw = source.raw()
        return UserInfo(
            id = raw.id,
            name = raw.name,
            contact = contact,
            createdAt = raw.createdAt!!,
            updatedAt = raw.updatedAt
        )
    }
}
