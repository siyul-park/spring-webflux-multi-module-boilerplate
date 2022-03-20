package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFinder
import io.github.siyual_park.persistence.Aggregate
import io.github.siyual_park.user.entity.User
import io.github.siyual_park.user.repository.UserCredentialRepository
import io.github.siyual_park.user.repository.UserRepository
import io.github.siyual_park.user.repository.UserScopeRepository
import org.springframework.transaction.reactive.TransactionalOperator

class UserAggregate(
    value: User,
    private val userRepository: UserRepository,
    private val userCredentialRepository: UserCredentialRepository,
    private val userScopeRepository: UserScopeRepository,
    private val scopeTokenFinder: ScopeTokenFinder,
    private val operator: TransactionalOperator,
) : Aggregate<User, Long>(value, userRepository) {
    var name: String
        get() = root[User::name]
        set(value) { root[User::name] = value }
}
