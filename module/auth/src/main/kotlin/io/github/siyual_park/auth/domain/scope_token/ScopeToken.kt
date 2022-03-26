package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.auth.domain.authorization.Authorizable
import io.github.siyual_park.auth.entity.ScopeRelationData
import io.github.siyual_park.auth.entity.ScopeTokenData
import io.github.siyual_park.auth.repository.ScopeRelationRepository
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.data.event.AfterDeleteEvent
import io.github.siyual_park.data.event.BeforeDeleteEvent
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.repository.r2dbc.findOneOrFail
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.persistence.Persistence
import io.github.siyual_park.persistence.proxy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

class ScopeToken(
    value: ScopeTokenData,
    private val scopeTokenRepository: ScopeTokenRepository,
    private val scopeRelationRepository: ScopeRelationRepository,
    private val operator: TransactionalOperator,
    private val eventPublisher: EventPublisher,
) : Persistence<ScopeTokenData, Long>(value, scopeTokenRepository, eventPublisher), Authorizable {
    private val scopeTokenMapper = ScopeTokenMapper(
        scopeTokenRepository,
        scopeRelationRepository,
        operator,
        eventPublisher
    )
    private val scopeTokenStorage = ScopeTokenStorage(scopeTokenRepository, scopeTokenMapper)

    val id: Long
        get() = root[ScopeTokenData::id] ?: throw EmptyResultDataAccessException(1)
    var name by proxy(root, ScopeTokenData::name)
    var description by proxy(root, ScopeTokenData::description)

    override suspend fun has(scopeToken: ScopeToken): Boolean {
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
            throw UnsupportedOperationException("Scope[$id] is not support pack operator")
        }

        scopeRelationRepository.create(
            ScopeRelationData(
                parentId = id,
                childId = scopeToken.id
            )
        )
    }

    override suspend fun revoke(scopeToken: ScopeToken) {
        if (!isPacked()) {
            throw UnsupportedOperationException("Scope[$id] is not support pack operator")
        }

        val scopeTokenRelation = scopeRelationRepository.findOneOrFail(
            where(ScopeRelationData::parentId).`is`(id)
                .and(where(ScopeRelationData::childId).`is`(scopeToken.id))
        )
        scopeRelationRepository.delete(scopeTokenRelation)
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
            if (!isPacked()) {
                throw UnsupportedOperationException("Scope[$id] is not support pack operator")
            }

            val relations = scopeRelationRepository.findAllByParentId(id)
            scopeTokenStorage.load(relations.map { it.childId }.toList())
                .collect { emit(it) }
        }
    }

    fun isPacked(): Boolean {
        return name.endsWith(":pack")
    }

    fun isSystem(): Boolean {
        return root[ScopeTokenData::system]
    }

    override suspend fun clear() {
        operator.executeAndAwait {
            eventPublisher.publish(BeforeDeleteEvent(this))
            scopeRelationRepository.deleteAllByChildId(id)
            scopeRelationRepository.deleteAllByParentId(id)
            scopeTokenRepository.delete(root.raw())
            root.clear()
            eventPublisher.publish(AfterDeleteEvent(this))
        }
    }
}
