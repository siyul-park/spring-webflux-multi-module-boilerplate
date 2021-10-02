package io.github.siyual_park.auth.domain

import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.auth.repository.ScopeRelationRepository
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class ScopeTokenFinder(
    private val scopeTokenRepository: ScopeTokenRepository,
    private val scopeRelationRepository: ScopeRelationRepository
) {
    suspend fun findAllByParent(parentName: String): Flow<ScopeToken> {
        return scopeTokenRepository.findByName(parentName)?.let { findAllByParent(it) } ?: emptyFlow()
    }

    suspend fun findAllByParent(parent: ScopeToken): Flow<ScopeToken> {
        val relations = scopeRelationRepository.findAllByParent(parent)
        return scopeTokenRepository.findAllById(relations.map { it.childId }.toList())
    }
}
