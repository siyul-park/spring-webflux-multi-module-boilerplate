package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.data.aggregation.FetchContextProvider
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.user.entity.UserData
import io.github.siyual_park.user.repository.UserCredentialRepository
import io.github.siyual_park.user.repository.UserRepository
import io.github.siyual_park.user.repository.UserScopeRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator

@Component
class UsersMapper(
    private val userRepository: UserRepository,
    private val userCredentialRepository: UserCredentialRepository,
    private val userScopeRepository: UserScopeRepository,
    private val scopeTokenStorage: ScopeTokenStorage,
    private val operator: TransactionalOperator,
    private val eventPublisher: EventPublisher
) : Mapper<Collection<UserData>, Collection<User>> {
    override val sourceType = object : TypeReference<Collection<UserData>>() {}
    override val targetType = object : TypeReference<Collection<User>>() {}

    override suspend fun map(source: Collection<UserData>): Collection<User> {
        val fetchContextProvider = FetchContextProvider()

        return source.map {
            User(
                it,
                userRepository,
                userCredentialRepository,
                userScopeRepository,
                scopeTokenStorage,
                fetchContextProvider,
                operator,
                eventPublisher
            )
        }
    }
}
