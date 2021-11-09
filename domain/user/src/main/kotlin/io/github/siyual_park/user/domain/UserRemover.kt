package io.github.siyual_park.user.domain

import io.github.siyual_park.data.repository.updateById
import io.github.siyual_park.user.entity.User
import io.github.siyual_park.user.repository.UserCredentialRepository
import io.github.siyual_park.user.repository.UserRepository
import io.github.siyual_park.user.repository.UserScopeRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.Instant

@Component
class UserRemover(
    private val userRepository: UserRepository,
    private val userCredentialRepository: UserCredentialRepository,
    private val userScopeRepository: UserScopeRepository,
    private val operator: TransactionalOperator,
) {
    suspend fun remove(user: User, soft: Boolean = true) {
        user.id?.let { remove(it, soft) }
    }

    suspend fun remove(userId: Long, soft: Boolean = true) = operator.executeAndAwait {
        userScopeRepository.deleteAllByUserId(userId)
        userCredentialRepository.deleteByUserId(userId)
        if (!soft) {
            userRepository.deleteById(userId)
        } else {
            userRepository.updateById(userId) {
                if (it.deletedAt == null) {
                    it.deletedAt = Instant.now()
                }
            }
        }
    }
}
