package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.ScopeTokenInfo
import io.github.siyual_park.application.server.dto.response.UserInfo
import io.github.siyual_park.auth.domain.authorization.Authorizator
import io.github.siyual_park.auth.domain.getPrincipal
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.mapper.map
import io.github.siyual_park.user.domain.User
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class UserInfoMapper(
    private val mapperContext: MapperContext,
    private val authorizator: Authorizator,
) : Mapper<User, UserInfo> {
    override val sourceType = object : TypeReference<User>() {}
    override val targetType = object : TypeReference<UserInfo>() {}

    override suspend fun map(source: User): UserInfo {
        val principal = getPrincipal()
        val scope: Collection<ScopeTokenInfo>? = if (
            principal != null &&
            authorizator.authorize(
                principal,
                listOf("users[self].scope:read", "users.scope:read"),
                listOf(source.id, null)
            )
        ) {
            mapperContext.map(source.getScope(deep = false).toList() as Collection<ScopeToken>)
        } else {
            null
        }

        val raw = source.raw()
        return UserInfo(
            id = raw.id,
            name = raw.name,
            email = raw.email,
            scope = scope,
            createdAt = raw.createdAt!!,
            updatedAt = raw.updatedAt
        )
    }
}
