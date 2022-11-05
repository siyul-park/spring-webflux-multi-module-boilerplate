package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.data.aggregation.FetchContext
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.user.entity.UserEntity
import io.github.siyual_park.user.repository.UserEntityRepository
import io.github.siyual_park.user.repository.UserScopeEntityRepository
import org.springframework.stereotype.Component

@Component
class UserMapper(
    private val userEntityRepository: UserEntityRepository,
    private val userScopeEntityRepository: UserScopeEntityRepository,
    private val scopeTokenStorage: ScopeTokenStorage
) : Mapper<UserEntity, User> {
    override val sourceType = object : TypeReference<UserEntity>() {}
    override val targetType = object : TypeReference<User>() {}

    override suspend fun map(source: UserEntity): User {
        val fetchContext = FetchContext()

        return User(
            source,
            userEntityRepository,
            userScopeEntityRepository,
            scopeTokenStorage,
            fetchContext
        )
    }
}
