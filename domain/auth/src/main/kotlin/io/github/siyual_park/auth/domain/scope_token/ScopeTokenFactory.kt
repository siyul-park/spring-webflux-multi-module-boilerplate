package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.auth.entity.ScopeTokenData
import io.github.siyual_park.auth.repository.ScopeTokenDataRepository
import org.springframework.stereotype.Component

@Component
class ScopeTokenFactory(
    private val scopeTokenDataRepository: ScopeTokenDataRepository,
    private val scopeTokenMapper: ScopeTokenMapper
) {
    private val scopeTokenStorage = ScopeTokenStorage(scopeTokenDataRepository, scopeTokenMapper)

    suspend fun upsert(name: String): ScopeToken {
        return upsert(CreateScopeTokenPayload(name = name))
    }

    suspend fun upsert(payload: CreateScopeTokenPayload): ScopeToken {
        val exited = scopeTokenStorage.load(payload.name)
        return if (exited != null) {
            exited.description = payload.description
            exited.sync()
            exited
        } else {
            create(payload)
        }
    }

    suspend fun create(payload: CreateScopeTokenPayload): ScopeToken {
        return scopeTokenDataRepository.create(
            ScopeTokenData(
                name = payload.name,
                description = payload.description,
                system = payload.system
            )
        )
            .let { scopeTokenMapper.map(it) }
    }
}
