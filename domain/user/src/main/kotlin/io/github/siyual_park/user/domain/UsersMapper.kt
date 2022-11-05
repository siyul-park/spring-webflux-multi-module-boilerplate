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
class UsersMapper(
    private val userEntityRepository: UserEntityRepository,
    private val userScopeEntityRepository: UserScopeEntityRepository,
    private val scopeTokenStorage: ScopeTokenStorage
) : Mapper<Collection<UserEntity>, Collection<User>> {
    override val sourceType = object : TypeReference<Collection<UserEntity>>() {}
    override val targetType = object : TypeReference<Collection<User>>() {}

    override suspend fun map(source: Collection<UserEntity>): Collection<User> {
        val fetchContext = FetchContext()
        return source.map {
            User(
                it,
                userEntityRepository,
                userScopeEntityRepository,
                scopeTokenStorage,
                fetchContext
            )
        }
    }
}
