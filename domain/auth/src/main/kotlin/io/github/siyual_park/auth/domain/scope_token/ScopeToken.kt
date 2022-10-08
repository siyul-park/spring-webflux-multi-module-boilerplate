package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.auth.domain.authorization.Authorizable
import io.github.siyual_park.auth.entity.ScopeRelationData
import io.github.siyual_park.auth.entity.ScopeTokenData
import io.github.siyual_park.auth.repository.ScopeRelationDataRepository
import io.github.siyual_park.auth.repository.ScopeTokenDataRepository
import io.github.siyual_park.data.criteria.and
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.data.repository.findOneOrFail
import io.github.siyual_park.data.transaction.SuspendTransactionContextHolder
import io.github.siyual_park.persistence.Persistence
import io.github.siyual_park.persistence.PersistenceSynchronization
import io.github.siyual_park.persistence.proxy
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import java.util.Collections.synchronizedList

class ScopeToken(
    value: ScopeTokenData,
    private val scopeTokenDataRepository: ScopeTokenDataRepository,
    private val scopeRelationDataRepository: ScopeRelationDataRepository
) : Persistence<ScopeTokenData, ULID>(value, scopeTokenDataRepository), Authorizable {
    val id by proxy(root, ScopeTokenData::id)
    var name by proxy(root, ScopeTokenData::name)
    var description by proxy(root, ScopeTokenData::description)

    val createdAt by proxy(root, ScopeTokenData::createdAt)
    val updatedAt by proxy(root, ScopeTokenData::updatedAt)

    init {
        synchronize(object : PersistenceSynchronization {
            override suspend fun beforeClear() {
                scopeRelationDataRepository.deleteAllByChildId(id)
                scopeRelationDataRepository.deleteAllByParentId(id)
            }
        })
    }

    override suspend fun has(scopeToken: ScopeToken): Boolean {
        if (id == scopeToken.id) {
            return true
        }
        if (!this.isPacked()) {
            return false
        }

        return scopeRelationDataRepository.exists(
            where(ScopeRelationData::parentId).`is`(id)
                .and(where(ScopeRelationData::childId).`is`(scopeToken.id))
        )
    }

    override suspend fun grant(scopeToken: ScopeToken) {
        if (!isPacked()) {
            throw UnsupportedOperationException("Scope[$id] is not support pack operator")
        }

        scopeRelationDataRepository.create(
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

        val scopeTokenRelation = scopeRelationDataRepository.findOneOrFail(
            where(ScopeRelationData::parentId).`is`(id)
                .and(where(ScopeRelationData::childId).`is`(scopeToken.id))
        )
        scopeRelationDataRepository.delete(scopeTokenRelation)
    }

    fun relations(): Flow<Pair<ScopeToken, ScopeToken>> {
        if (!isPacked()) {
            return emptyFlow()
        }

        val self = this
        return flow {
            val task = synchronizedList(mutableListOf<ScopeToken>())

            val store = synchronizedList(mutableListOf<ScopeToken>())
            val relations = synchronizedList(mutableListOf<ScopeRelationData>())

            task.add(self)

            while (task.isNotEmpty()) {
                store.addAll(task)
                val packed = task.filter { it.isPacked() }
                task.clear()

                if (packed.isNotEmpty()) {
                    val currentRelations = scopeRelationDataRepository.findAllByParentId(packed.map { it.id }).toList()
                    relations.addAll(currentRelations)

                    scopeTokenDataRepository.findAllById(currentRelations.map { it.childId }.toList())
                        .map { ScopeToken(it, scopeTokenDataRepository, scopeRelationDataRepository) }
                        .collect { task.add(it) }
                }
            }

            val context = SuspendTransactionContextHolder.getContext()
            store
                .onEach { if (context != null) it.link() }
                .forEach { scopeToken ->
                    val parents = relations.filter { it.childId == scopeToken.id }.map { r -> store.find { it.id == r.parentId } }.filterNotNull()
                    val children = relations.filter { it.parentId == scopeToken.id }.map { r -> store.find { it.id == r.childId } }.filterNotNull()

                    parents.forEach {
                        emit(it to scopeToken)
                    }
                    children.forEach {
                        emit(scopeToken to it)
                    }
                }
        }
    }

    fun resolve(): Flow<ScopeToken> {
        val self = this
        return flow {
            if (!isPacked()) {
                emit(self)
            } else {
                val task = synchronizedList(mutableListOf<ScopeToken>())
                children().collect { task.add(it) }

                val context = SuspendTransactionContextHolder.getContext()
                while (task.isNotEmpty()) {
                    val packed = task.filter { it.isPacked() }
                    val notPacked = task.filter { !it.isPacked() }

                    task.clear()

                    notPacked.onEach { if (context != null) it.link() }.forEach { emit(it) }
                    if (packed.isNotEmpty()) {
                        val relations = scopeRelationDataRepository.findAllByParentId(packed.map { it.id })
                        scopeTokenDataRepository.findAllById(relations.map { it.childId }.toList())
                            .map { ScopeToken(it, scopeTokenDataRepository, scopeRelationDataRepository) }
                            .collect { task.add(it) }
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
            val context = SuspendTransactionContextHolder.getContext()
            val relations = scopeRelationDataRepository.findAllByParentId(id)
            scopeTokenDataRepository.findAllById(relations.map { it.childId }.toList())
                .map { ScopeToken(it, scopeTokenDataRepository, scopeRelationDataRepository) }
                .onEach { if (context != null) it.link() }
                .collect { emit(it) }
        }
    }

    fun isPacked(): Boolean {
        return name.endsWith(":pack")
    }

    fun isSystem(): Boolean {
        return root[ScopeTokenData::system]
    }
}
