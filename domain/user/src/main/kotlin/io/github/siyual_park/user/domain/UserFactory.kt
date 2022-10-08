package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.hash
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.data.cache.SuspendLazy
import io.github.siyual_park.user.entity.UserCredentialData
import io.github.siyual_park.user.entity.UserData
import io.github.siyual_park.user.repository.UserCredentialDataRepository
import io.github.siyual_park.user.repository.UserDataRepository
import io.github.siyual_park.user.repository.UserScopeDataRepository
import org.springframework.stereotype.Component
import java.security.MessageDigest

@Component
class UserFactory(
    private val userDataRepository: UserDataRepository,
    private val userCredentialDataRepository: UserCredentialDataRepository,
    userScopeDataRepository: UserScopeDataRepository,
    private val scopeTokenStorage: ScopeTokenStorage,
    private val hashAlgorithm: String = "SHA-256"
) {
    private val userMapper = UserMapper(userDataRepository, userCredentialDataRepository, userScopeDataRepository, scopeTokenStorage)

    private val defaultScope = SuspendLazy {
        scopeTokenStorage.loadOrFail("user:pack")
    }

    suspend fun create(payload: CreateUserPayload): User {
        val user = createUser(payload)

        user.link()
        createCredential(user, payload)

        if (payload.scope == null) {
            val scope = defaultScope.get()
            user.grant(scope)
        } else {
            payload.scope.forEach {
                user.grant(it)
            }
        }

        return user
    }

    private suspend fun createUser(payload: CreateUserPayload): User {
        return UserData(payload.name, payload.email)
            .let { userDataRepository.create(it) }
            .let { userMapper.map(it) }
    }

    private suspend fun createCredential(user: User, payload: CreateUserPayload): UserCredentialData {
        val messageDigest = MessageDigest.getInstance(hashAlgorithm)
        val password = messageDigest.hash(payload.password)

        return userCredentialDataRepository.create(
            UserCredentialData(
                userId = user.id,
                password = password,
                hashAlgorithm = hashAlgorithm
            )
        )
    }
}
