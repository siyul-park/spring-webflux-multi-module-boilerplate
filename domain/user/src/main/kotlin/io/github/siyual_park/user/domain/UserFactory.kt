package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.hash
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.entity.ScopeTokenData
import io.github.siyual_park.data.event.AfterSaveEvent
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.persistence.loadOrFail
import io.github.siyual_park.user.entity.UserCredentialData
import io.github.siyual_park.user.entity.UserData
import io.github.siyual_park.user.entity.UserScopeData
import io.github.siyual_park.user.repository.UserCredentialRepository
import io.github.siyual_park.user.repository.UserRepository
import io.github.siyual_park.user.repository.UserScopeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.security.MessageDigest

@Component
class UserFactory(
    private val userRepository: UserRepository,
    private val userCredentialRepository: UserCredentialRepository,
    private val userScopeRepository: UserScopeRepository,
    private val userMapper: UserMapper,
    private val scopeTokenStorage: ScopeTokenStorage,
    private val operator: TransactionalOperator,
    private val eventPublisher: EventPublisher,
    private val hashAlgorithm: String = "SHA-256"
) {
    suspend fun create(payload: CreateUserPayload): User =
        operator.executeAndAwait {
            createUser(payload).also {
                createCredential(it, payload)
                if (payload.scope == null) {
                    createDefaultScope(it).collect()
                } else {
                    createScope(it, payload.scope).collect()
                }
            }
                .let { userMapper.map(it) }
                .also { it.link() }
                .also { eventPublisher.publish(AfterSaveEvent(it)) }
        }!!

    private suspend fun createUser(payload: CreateUserPayload): UserData {
        return userRepository.create(UserData(payload.name))
    }

    private suspend fun createCredential(user: UserData, payload: CreateUserPayload): UserCredentialData {
        val messageDigest = MessageDigest.getInstance(hashAlgorithm)
        val password = messageDigest.hash(payload.password)

        return userCredentialRepository.create(
            UserCredentialData(
                userId = user.id!!,
                password = password,
                hashAlgorithm = hashAlgorithm
            )
        )
    }

    private fun createDefaultScope(user: UserData): Flow<UserScopeData> {
        return flow {
            createScope(
                user,
                listOf(scopeTokenStorage.loadOrFail(where(ScopeTokenData::name).`is`("user:pack")))
            )
        }
    }

    private fun createScope(user: UserData, scope: Collection<ScopeToken>): Flow<UserScopeData> {
        return userScopeRepository.createAll(
            scope.filter { it.id != null }
                .map {
                    UserScopeData(
                        userId = user.id!!,
                        scopeTokenId = it.id!!
                    )
                }
        )
    }
}
