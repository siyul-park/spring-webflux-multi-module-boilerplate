package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.persistence.QueryStorage
import io.github.siyual_park.persistence.SimpleQueryStorage
import io.github.siyual_park.ulid.ULID
import io.github.siyual_park.user.entity.UserData
import io.github.siyual_park.user.repository.UserCredentialDataRepository
import io.github.siyual_park.user.repository.UserDataRepository
import io.github.siyual_park.user.repository.UserScopeDataRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Component

@Component
class UserStorage(
    private val userDataRepository: UserDataRepository,
    private val userCredentialDataRepository: UserCredentialDataRepository,
    private val userScopeDataRepository: UserScopeDataRepository,
    private val scopeTokenStorage: ScopeTokenStorage
) : QueryStorage<User, ULID> by SimpleQueryStorage(
    userDataRepository,
    UserMapper(userDataRepository, userCredentialDataRepository, userScopeDataRepository, scopeTokenStorage).let { mapper -> { mapper.map(it) } },
    UsersMapper(userDataRepository, userCredentialDataRepository, userScopeDataRepository, scopeTokenStorage).let { mapper -> { mapper.map(it) } },
) {
    suspend fun load(name: String): User? {
        return load(where(UserData::name).`is`(name))
    }
}

suspend fun UserStorage.loadOrFail(name: String): User {
    return load(name) ?: throw EmptyResultDataAccessException(1)
}
