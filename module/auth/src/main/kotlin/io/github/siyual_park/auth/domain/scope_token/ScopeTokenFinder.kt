package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.auth.repository.ScopeRelationRepository
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.search.finder.Finder
import io.github.siyual_park.search.finder.FinderAdapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Component

@Component
class ScopeTokenFinder(
    private val scopeTokenRepository: ScopeTokenRepository,
    private val scopeRelationRepository: ScopeRelationRepository
) : Finder<ScopeToken, Long> by FinderAdapter(scopeTokenRepository) {
    fun findAllByParent(parentName: String): Flow<ScopeToken> {
        return flow {
            scopeTokenRepository.findByName(parentName)
                ?.let { emitAll(findAllByParent(it)) }
        }
    }

    fun findAllByParent(parent: ScopeToken): Flow<ScopeToken> {
        return flow {
            val relations = scopeRelationRepository.findAllByParent(parent)
            emitAll(findAllById(relations.map { it.childId }.toList()))
        }
    }

    fun findAllByName(names: Iterable<String>): Flow<ScopeToken> {
        return scopeTokenRepository.findAllByName(names)
    }

    suspend fun findByNameOrFail(name: String): ScopeToken {
        return findByName(name) ?: throw EmptyResultDataAccessException(1)
    }

    suspend fun findByName(name: String): ScopeToken? {
        return scopeTokenRepository.findByName(name)
    }
}
