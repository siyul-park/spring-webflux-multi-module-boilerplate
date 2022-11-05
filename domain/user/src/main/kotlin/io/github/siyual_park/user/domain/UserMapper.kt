package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.data.aggregation.FetchContext
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.user.entity.UserData
import io.github.siyual_park.user.repository.UserDataRepository
import io.github.siyual_park.user.repository.UserScopeDataRepository
import org.springframework.stereotype.Component

@Component
class UserMapper(
    private val userDataRepository: UserDataRepository,
    private val userScopeDataRepository: UserScopeDataRepository,
    private val scopeTokenStorage: ScopeTokenStorage
) : Mapper<UserData, User> {
    override val sourceType = object : TypeReference<UserData>() {}
    override val targetType = object : TypeReference<User>() {}

    override suspend fun map(source: UserData): User {
        val fetchContext = FetchContext()

        return User(
            source,
            userDataRepository,
            userScopeDataRepository,
            scopeTokenStorage,
            fetchContext
        )
    }
}
