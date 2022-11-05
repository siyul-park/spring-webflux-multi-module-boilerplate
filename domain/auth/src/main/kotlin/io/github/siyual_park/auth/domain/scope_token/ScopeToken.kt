package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.auth.domain.authorization.Authorizable
import io.github.siyual_park.auth.entity.ScopeRelationEntity
import io.github.siyual_park.auth.entity.ScopeTokenEntity
import io.github.siyual_park.auth.repository.ScopeRelationEntityRepository
import io.github.siyual_park.auth.repository.ScopeTokenEntityRepository
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
    entity: ScopeTokenEntity,
    private val scopeTokenEntityRepository: ScopeTokenEntityRepository,
    private val scopeRelationEntityRepository: ScopeRelationEntityRepository
) : Persistence<ScopeTokenEntity, ULID>(entity, scopeTokenEntityRepository), Authorizable {
    val id by proxy(root, ScopeTokenEntity::id)
    var name by proxy(root, ScopeTokenEntity::name)
    var description by proxy(root, ScopeTokenEntity::description)

    val createdAt by proxy(root, ScopeTokenEntity::createdAt)
    val updatedAt by proxy(root, ScopeTokenEntity::updatedAt)

    init {
        synchronize(object : PersistenceSynchronization {
            override suspend fun beforeClear() {
                scopeRelationEntityRepository.deleteAllByChildId(id)
                scopeRelationEntityRepository.deleteAllByParentId(id)
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

        return scopeRelationEntityRepository.exists(
            where(ScopeRelationEntity::parentId).`is`(id)
                .and(where(ScopeRelationEntity::childId).`is`(scopeToken.id))
        )
    }

    override suspend fun grant(scopeToken: ScopeToken) {
        if (!isPacked()) {
            throw UnsupportedOperationException("Scope[$id] is not support pack operator")
        }

        scopeRelationEntityRepository.create(
            ScopeRelationEntity(
                parentId = id,
                childId = scopeToken.id
            )
        )
    }

    override suspend fun revoke(scopeToken: ScopeToken) {
        if (!isPacked()) {
            throw UnsupportedOperationException("Scope[$id] is not support pack operator")
        }

        val scopeTokenRelation = scopeRelationEntityRepository.findOneOrFail(
            where(ScopeRelationEntity::parentId).`is`(id)
                .and(where(ScopeRelationEntity::childId).`is`(scopeToken.id))
        )
        scopeRelationEntityRepository.delete(scopeTokenRelation)
    }

    fun relations(): Flow<Pair<ScopeToken, ScopeToken>> {
        if (!isPacked()) {
            return emptyFlow()
        }

        val self = this
        return flow {
            val task = synchronizedList(mutableListOf<ScopeToken>())

            val store = synchronizedList(mutableListOf<ScopeToken>())
            val relations = synchronizedList(mutableListOf<ScopeRelationEntity>())

            task.add(self)

            while (task.isNotEmpty()) {
                store.addAll(task)
                val packed = task.filter { it.isPacked() }
                task.clear()

                if (packed.isNotEmpty()) {
                    val currentRelations = scopeRelationEntityRepository.findAllByParentId(packed.map { it.id }).toList()
                    relations.addAll(currentRelations)

                    scopeTokenEntityRepository.findAllById(currentRelations.map { it.childId }.toList())
                        .map { ScopeToken(it, scopeTokenEntityRepository, scopeRelationEntityRepository) }
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
                        val relations = scopeRelationEntityRepository.findAllByParentId(packed.map { it.id })
                        scopeTokenEntityRepository.findAllById(relations.map { it.childId }.toList())
                            .map { ScopeToken(it, scopeTokenEntityRepository, scopeRelationEntityRepository) }
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
            val relations = scopeRelationEntityRepository.findAllByParentId(id)
            scopeTokenEntityRepository.findAllById(relations.map { it.childId }.toList())
                .map { ScopeToken(it, scopeTokenEntityRepository, scopeRelationEntityRepository) }
                .onEach { if (context != null) it.link() }
                .collect { emit(it) }
        }
    }

    fun isPacked(): Boolean {
        return name.endsWith(":pack")
    }

    fun isSystem(): Boolean {
        return root[ScopeTokenEntity::system]
    }
}
