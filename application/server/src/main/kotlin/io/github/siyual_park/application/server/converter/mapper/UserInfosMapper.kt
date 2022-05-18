package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.UserInfo
import io.github.siyual_park.auth.domain.authorization.Authorizator
import io.github.siyual_park.auth.domain.getPrincipal
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.mapper.map
import io.github.siyual_park.persistence.AsyncLazy
import io.github.siyual_park.user.domain.User
import io.github.siyual_park.user.repository.UserContactRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class UserInfosMapper(
    private val mapperContext: MapperContext,
    private val authorizator: Authorizator,
    private val scopeTokenStorage: ScopeTokenStorage,
    private val userContactRepository: UserContactRepository
) : Mapper<Collection<User>, Collection<UserInfo>> {
    override val sourceType = object : TypeReference<Collection<User>>() {}
    override val targetType = object : TypeReference<Collection<UserInfo>>() {}

    private val contactSelfReadScopeToken = AsyncLazy {
        scopeTokenStorage.loadOrFail("users[self].contact:read")
    }
    private val contactReadScopeToken = AsyncLazy {
        scopeTokenStorage.loadOrFail("users.contact:read")
    }

    override suspend fun map(source: Collection<User>): Collection<UserInfo> {
        val principal = getPrincipal()
        val canContactInfoRead = principal?.let {
            authorizator.authorize(it, contactReadScopeToken.get(), null)
        } ?: false

        val readContactUserIds = source.mapNotNull { user ->
            val localCanContactInfoRead = if (canContactInfoRead) {
                true
            } else {
                principal?.let {
                    authorizator.authorize(it, contactSelfReadScopeToken.get(), user.id)
                } ?: false
            }

            if (localCanContactInfoRead) {
                user.id
            } else {
                null
            }
        }

        val contacts = userContactRepository.findAllByUserId(readContactUserIds)
            .toList()
            .associateBy { it.userId }

        return source.map { user ->
            val raw = user.raw()
            UserInfo(
                id = raw.id,
                name = raw.name,
                contact = contacts[user.id]?.let { mapperContext.map(it) },
                createdAt = raw.createdAt!!,
                updatedAt = raw.updatedAt
            )
        }
    }
}
