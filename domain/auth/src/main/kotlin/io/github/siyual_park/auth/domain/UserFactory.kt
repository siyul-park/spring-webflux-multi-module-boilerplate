package io.github.siyual_park.auth.domain

import io.github.siyual_park.auth.entity.User
import io.github.siyual_park.auth.entity.UserAuthInfo
import io.github.siyual_park.auth.entity.UserScope
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.auth.repository.UserAuthInfoRepository
import io.github.siyual_park.auth.repository.UserRepository
import io.github.siyual_park.auth.repository.UserScopeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.security.MessageDigest

@Component
class UserFactory(
    private val userRepository: UserRepository,
    private val scopeTokenRepository: ScopeTokenRepository,
    private val userAuthInfoRepository: UserAuthInfoRepository,
    private val userScopeRepository: UserScopeRepository,
    private val operator: TransactionalOperator,
    private val hashAlgorithm: String = "SHA-256"
) {
    suspend fun create(payload: CreateUserPayload): User = operator.executeAndAwait {
        createUser(payload).also {
            createUserAuthInfo(it, payload)
            createDefaultUserScopes(it).toList()
        }
    }!!

    private suspend fun createUser(payload: CreateUserPayload): User {
        return userRepository.create(User(payload.username))
    }

    private suspend fun createUserAuthInfo(user: User, payload: CreateUserPayload): UserAuthInfo {
        val messageDigest = MessageDigest.getInstance(hashAlgorithm)
        val password = messageDigest.hash(payload.password)

        return userAuthInfoRepository.create(
            UserAuthInfo(
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
