package io.github.siyual_park.auth.domain

import io.github.siyual_park.auth.entity.User
import io.github.siyual_park.auth.entity.UserCredential
import io.github.siyual_park.auth.entity.UserScope
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.auth.repository.UserCredentialRepository
import io.github.siyual_park.auth.repository.UserRepository
import io.github.siyual_park.auth.repository.UserScopeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
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
    suspend fun create(payload: CreateUserPayload): User = operator.executeAndAwait {
        createUser(payload).also {
            createUserCredential(it, payload)
            createDefaultUserScopes(it).collect()
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
}
