package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.auth.entity.ScopeTokenData
import io.github.siyual_park.auth.repository.ScopeRelationRepository
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.data.event.AfterSaveEvent
import io.github.siyual_park.event.EventPublisher
import org.springframework.stereotype.Component

@Component
class ScopeTokenFactory(
    private val scopeTokenRepository: ScopeTokenRepository,
    private val scopeRelationRepository: ScopeRelationRepository,
    private val scopeTokenStorage: ScopeTokenStorage,
    private val eventPublisher: EventPublisher,
) {

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
        return scopeTokenRepository.create(
            ScopeTokenData(
                name = payload.name,
                description = payload.description
            )
        ).let { ScopeToken(it, scopeTokenRepository, scopeRelationRepository, eventPublisher) }
            .also { eventPublisher.publish(AfterSaveEvent(it)) }
    }
}
