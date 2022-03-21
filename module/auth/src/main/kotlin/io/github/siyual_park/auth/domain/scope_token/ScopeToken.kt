package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.auth.domain.authorization.Authorizable
import io.github.siyual_park.auth.entity.ScopeRelationData
import io.github.siyual_park.auth.entity.ScopeTokenData
import io.github.siyual_park.auth.repository.ScopeRelationRepository
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.data.event.AfterSaveEvent
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.persistence.Persistence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.dao.EmptyResultDataAccessException

class ScopeToken(
    value: ScopeTokenData,
    scopeTokenRepository: ScopeTokenRepository,
    private val scopeRelationRepository: ScopeRelationRepository,
    private val eventPublisher: EventPublisher,
) : Persistence<ScopeTokenData, Long>(value, scopeTokenRepository), Authorizable {
    private val scopeTokenMapper = ScopeTokenMapper(scopeTokenRepository, scopeRelationRepository, eventPublisher)
    private val scopeTokenStorage = ScopeTokenStorage(scopeTokenRepository, scopeTokenMapper)

    val id: Long
        get() = root[ScopeTokenData::id] ?: throw EmptyResultDataAccessException(1)

    var name: String
        get() = root[ScopeTokenData::name]
        set(value) { root[ScopeTokenData::name] = value }

    var description: String?
        get() = root[ScopeTokenData::description]
        set(value) { root[ScopeTokenData::description] = value }

    fun isPacked(): Boolean {
        return name.endsWith(":pack")
    }

    suspend fun has(scopeToken: ScopeToken): Boolean {
        if (id == scopeToken.id) {
            return true
        }
        if (!this.isPacked()) {
            return false
        }

        return scopeRelationRepository.exists(
            where(ScopeRelationData::parentId).`is`(id)
                .and(where(ScopeRelationData::childId).`is`(scopeToken.id))
        )
    }

    override suspend fun grant(scopeToken: ScopeToken) {
        if (!isPacked()) {
            return
        }

        scopeRelationRepository.create(
            ScopeRelationData(
                parentId = id,
                childId = scopeToken.id
            )
        )
            .also { eventPublisher.publish(AfterSaveEvent(it)) }
    }

    override suspend fun revoke(scopeToken: ScopeToken) {
        if (!isPacked()) {
            return
        }

        scopeRelationRepository.deleteAll(
            where(ScopeRelationData::parentId).`is`(id)
                .and(where(ScopeRelationData::childId).`is`(scopeToken.id))
        )
    }

    fun resolve(): Flow<ScopeToken> {
        val self = this
        return flow {
            if (!isPacked()) {
                emit(self)
            } else {
                children()
                    .collect {
                        if (it.isPacked()) {
                            emitAll(it.resolve())
                        } else {
                            emit(it)
                        }
                    }
            }
        }
    }

    fun children(): Flow<ScopeToken> {
        return flow {
            val relations = scopeRelationRepository.findAllByParent(root.raw())
            scopeTokenStorage.load(relations.map { it.childId }.toList())
                .collect { emit(it) }
        }
    }
}
