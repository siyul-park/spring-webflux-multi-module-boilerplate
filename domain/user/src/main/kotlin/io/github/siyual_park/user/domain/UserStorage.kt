package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.hash
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.data.cache.SuspendLazy
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.persistence.QueryableLoader
import io.github.siyual_park.persistence.SimpleQueryableLoader
import io.github.siyual_park.ulid.ULID
import io.github.siyual_park.user.entity.UserEntity
import io.github.siyual_park.user.repository.UserEntityRepository
import io.github.siyual_park.user.repository.UserScopeEntityRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Component
import java.security.MessageDigest

@Component
class UserStorage(
    private val userEntityRepository: UserEntityRepository,
    private val userScopeEntityRepository: UserScopeEntityRepository,
    private val scopeTokenStorage: ScopeTokenStorage,
    private val hashAlgorithm: String = "SHA-256"
) : QueryableLoader<User, ULID> by SimpleQueryableLoader(
    userEntityRepository,
    UserMapper(userEntityRepository, userScopeEntityRepository, scopeTokenStorage).let { mapper -> { mapper.map(it) } },
    UsersMapper(userEntityRepository, userScopeEntityRepository, scopeTokenStorage).let { mapper -> { mapper.map(it) } },
) {
    private val userMapper = UserMapper(userEntityRepository, userScopeEntityRepository, scopeTokenStorage)

    private val defaultScope = SuspendLazy {
        scopeTokenStorage.loadOrFail("user:pack")
    }

    suspend fun save(payload: CreateUserPayload): User {
        val messageDigest = MessageDigest.getInstance(hashAlgorithm)
        val user = UserEntity(
            name = payload.name,
            email = payload.email,
            hashAlgorithm = hashAlgorithm,
            password = messageDigest.hash(payload.password)
        )
            .let { userEntityRepository.create(it) }
            .let { userMapper.map(it) }
            .also { it.link() }

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

    suspend fun load(name: String): User? {
        return load(where(UserEntity::name).`is`(name))
    }
}

suspend fun UserStorage.loadOrFail(name: String): User {
    return load(name) ?: throw EmptyResultDataAccessException(1)
}
