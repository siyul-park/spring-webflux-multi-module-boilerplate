package io.github.siyual_park.auth.repository

import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.data.repository.Repository
import io.github.siyual_park.data.repository.in_memory.InMemoryRepository
import io.github.siyual_park.data.repository.in_memory.callback.EntityCallbacks
import io.github.siyual_park.data.repository.in_memory.generator.LongIdGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Component

class ScopeTokenRepositoryNameIndex : EntityCallbacks<ScopeToken> {
    private val index = mutableMapOf<String, Long>()

    override fun onCreate(entity: ScopeToken) {
        if (index.containsKey(entity.name)) {
            throw DuplicateKeyException("name: ${entity.name} is already existed")
        }
        index[entity.name] = entity.id!!
    }

    override fun onDelete(entity: ScopeToken) {
        index.remove(entity.name)
    }

    override fun onUpdate(origin: ScopeToken, entity: ScopeToken) {
        if (origin.name != entity.name || origin.id != entity.id) {
            index.remove(origin.name)
            index[entity.name] = entity.id!!
        }
    }

    operator fun get(key: String): Long? {
        return index[key]
    }
}

@Component
class ScopeTokenRepository(
    private val index: ScopeTokenRepositoryNameIndex = ScopeTokenRepositoryNameIndex()
) : Repository<ScopeToken, Long> by InMemoryRepository(
    ScopeToken::class,
    LongIdGenerator(),
    index
) {
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
}
