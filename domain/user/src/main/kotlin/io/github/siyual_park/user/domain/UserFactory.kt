package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.hash
import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.user.entity.User
import io.github.siyual_park.user.entity.UserCredential
import io.github.siyual_park.user.entity.UserScope
import io.github.siyual_park.user.repository.UserCredentialRepository
import io.github.siyual_park.user.repository.UserRepository
import io.github.siyual_park.user.repository.UserScopeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.security.MessageDigest

@Component
class UserFactory(
    private val userRepository: UserRepository,
    private val scopeTokenRepository: ScopeTokenRepository,
    private val userCredentialRepository: UserCredentialRepository,
    private val userScopeRepository: UserScopeRepository,
    private val operator: TransactionalOperator,
    private val hashAlgorithm: String = "SHA-256"
) {
    suspend fun create(payload: CreateUserPayload, scope: Collection<ScopeToken> = emptySet()): User = operator.executeAndAwait {
        createUser(payload).also {
            createUserCredential(it, payload)
            createDefaultUserScopes(it).collect()
            createAdditionalUserScopes(it, scope)
        }
    }!!

    private suspend fun createUser(payload: CreateUserPayload): User {
        return userRepository.create(User(payload.username))
    }

    private suspend fun createUserCredential(user: User, payload: CreateUserPayload): UserCredential {
        val messageDigest = MessageDigest.getInstance(hashAlgorithm)
        val password = messageDigest.hash(payload.password)

        return userCredentialRepository.create(
            UserCredential(
                userId = user.id!!,
                password = password,
                hashAlgorithm = hashAlgorithm
            )
        )
    }

    private fun createDefaultUserScopes(user: User): Flow<UserScope> {
        return scopeTokenRepository.findAllByDefault(true)
            .map {
                userScopeRepository.create(
                    UserScope(
                        userId = user.id!!,
                        scopeTokenId = it.id!!
                    )
                )
            }
    }

    private fun createAdditionalUserScopes(user: User, scope: Collection<ScopeToken>): Flow<UserScope> {
        return scope.asFlow()
            .filter { it.id != null }
            .map {
                userScopeRepository.create(
                    UserScope(
                        userId = user.id!!,
                        scopeTokenId = it.id!!
                    )
                )
            }
    }
}
