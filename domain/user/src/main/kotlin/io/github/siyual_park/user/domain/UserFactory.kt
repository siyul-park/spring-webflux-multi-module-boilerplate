package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.hash
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFinder
import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.data.callback.AfterSaveCallbacks
import io.github.siyual_park.user.entity.User
import io.github.siyual_park.user.entity.UserCredential
import io.github.siyual_park.user.entity.UserScope
import io.github.siyual_park.user.repository.UserCredentialRepository
import io.github.siyual_park.user.repository.UserRepository
import io.github.siyual_park.user.repository.UserScopeRepository
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
    private val userCredentialRepository: UserCredentialRepository,
    private val userScopeRepository: UserScopeRepository,
    private val scopeTokenFinder: ScopeTokenFinder,
    private val operator: TransactionalOperator,
    private val afterSaveCallbacks: AfterSaveCallbacks,
    private val hashAlgorithm: String = "SHA-256"
) {
    suspend fun create(payload: CreateUserPayload, scope: Collection<ScopeToken> = emptySet()): User =
        operator.executeAndAwait {
            createUser(payload).also {
                createUserCredential(it, payload)
                createDefaultUserScopes(it).collect()
                createAdditionalUserScopes(it, scope)
            }
        }!!.also {
            afterSaveCallbacks.onAfterSave(it)
        }

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

    private suspend fun createDefaultUserScopes(user: User): Flow<UserScope> {
        val userScope = scopeTokenFinder.findAllByParent("user")
        return userScopeRepository.createAll(
            userScope.map {
                UserScope(
                    userId = user.id!!,
                    scopeTokenId = it.id!!
                )
            }
        )
    }

    private fun createAdditionalUserScopes(user: User, scope: Collection<ScopeToken>): Flow<UserScope> {
        return userScopeRepository.createAll(
            scope.filter { it.id != null }
                .map {
                    UserScope(
                        userId = user.id!!,
                        scopeTokenId = it.id!!
                    )
                }
        )
    }
}
