package io.github.siyual_park.auth.repository

import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.data.repository.in_memory.InMemoryRepository
import io.github.siyual_park.data.repository.in_memory.callback.Index
import io.github.siyual_park.data.repository.in_memory.generator.LongIdGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Component

@Component
class ScopeTokenRepository : InMemoryRepository<ScopeToken, Long>(
    ScopeToken::class,
    LongIdGenerator(),
) {
    private val index = Index<ScopeToken, String, Long?>(ScopeToken::name, ScopeToken::id)

    init {
        entityCallbacks = index
    }

    suspend fun findByName(name: String): ScopeToken? {
        return index[name]?.let { findById(it) }
    }

    suspend fun findAllByName(names: Iterable<String>): Flow<ScopeToken> {
        return names.asFlow()
            .map { index[it] }
            .filterNotNull()
            .map { findById(it) }
            .filterNotNull()
    }

    suspend fun existsByName(name: String): Boolean {
        return index[name]?.let { existsById(it) } ?: false
    }
}
