package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.data.aggregation.FetchContext
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.user.entity.UserData
import io.github.siyual_park.user.repository.UserCredentialDataRepository
import io.github.siyual_park.user.repository.UserDataRepository
import io.github.siyual_park.user.repository.UserScopeDataRepository
import org.springframework.stereotype.Component

@Component
class UsersMapper(
    private val userDataRepository: UserDataRepository,
    private val userCredentialDataRepository: UserCredentialDataRepository,
    private val userScopeDataRepository: UserScopeDataRepository,
    private val scopeTokenStorage: ScopeTokenStorage
) : Mapper<Collection<UserData>, Collection<User>> {
    override val sourceType = object : TypeReference<Collection<UserData>>() {}
    override val targetType = object : TypeReference<Collection<User>>() {}

    override suspend fun map(source: Collection<UserData>): Collection<User> {
        val fetchContext = FetchContext()

        return source.map {
            User(
                it,
                userDataRepository,
                userCredentialDataRepository,
                userScopeDataRepository,
                scopeTokenStorage,
                fetchContext
            )
        }
    }
}
