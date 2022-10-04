package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.data.aggregation.FetchContext
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.user.entity.UserData
import io.github.siyual_park.user.repository.UserCredentialRepository
import io.github.siyual_park.user.repository.UserRepository
import io.github.siyual_park.user.repository.UserScopeRepository
import org.springframework.stereotype.Component

@Component
class UserMapper(
    private val userRepository: UserRepository,
    private val userCredentialRepository: UserCredentialRepository,
    private val userScopeRepository: UserScopeRepository,
    private val scopeTokenStorage: ScopeTokenStorage
) : Mapper<UserData, User> {
    override val sourceType = object : TypeReference<UserData>() {}
    override val targetType = object : TypeReference<User>() {}

    override suspend fun map(source: UserData): User {
        val fetchContext = FetchContext()

        return User(
            source,
            userRepository,
            userCredentialRepository,
            userScopeRepository,
            scopeTokenStorage,
            fetchContext
        )
    }
}
