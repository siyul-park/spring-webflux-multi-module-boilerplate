package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.auth.repository.ScopeRelationRepository
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.data.repository.cache.CacheIndex
import io.github.siyual_park.data.repository.cache.CacheLoader
import io.github.siyual_park.search.finder.R2dbcFinder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Component

@Component
class ScopeTokenFinder(
    private val scopeTokenRepository: ScopeTokenRepository,
    private val scopeRelationRepository: ScopeRelationRepository
) : R2dbcFinder<ScopeToken, Long>(scopeTokenRepository) {
    private val nameIndex = CacheIndex(
        loader = object : CacheLoader<ScopeToken, String>() {
            override suspend fun loadByKey(key: String): ScopeToken? {
                return scopeTokenRepository.findByName(key)
            }

            override fun loadByKeys(keys: Iterable<String>): Flow<ScopeToken?> {
                return flow {
                    val result = mutableMapOf<String, ScopeToken>()
                    scopeTokenRepository.findAllByName(keys).toList()
                        .forEach { result[it.name] = it }
                    keys.forEach { emit(result[it]) }
                }
            }
        }
    )

    suspend fun findAllByParent(parentName: String, cache: Boolean = false): Flow<ScopeToken> {
        return scopeTokenRepository.findByName(parentName)?.let { findAllByParent(it, cache) } ?: emptyFlow()
    }

    suspend fun findAllByParent(parent: ScopeToken, cache: Boolean = false): Flow<ScopeToken> {
        val relations = scopeRelationRepository.findAllByParent(parent)
        return findAllById(relations.map { it.childId }.toList(), cache)
    }

    fun findAllByName(names: Iterable<String>, cache: Boolean = false): Flow<ScopeToken> {
        return nameIndex.findAllByKey(names, cache)
    }

    suspend fun findByNameOrFail(name: String, cache: Boolean = false): ScopeToken {
        return findByName(name, cache) ?: throw EmptyResultDataAccessException(1)
    }

    suspend fun findByName(name: String, cache: Boolean = false): ScopeToken? {
        return nameIndex.findByKey(name, cache)
    }
}
