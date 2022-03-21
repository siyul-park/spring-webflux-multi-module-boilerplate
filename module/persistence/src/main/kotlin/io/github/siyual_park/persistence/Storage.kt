package io.github.siyual_park.persistence

import kotlinx.coroutines.flow.Flow
import org.springframework.dao.EmptyResultDataAccessException

interface Storage<T : Any, ID : Any, P : Persistence<T, ID>> {
    suspend fun load(id: ID): P?
    fun load(ids: Iterable<ID>): Flow<P>

    suspend fun count(): Long
}

suspend fun <T : Any, ID : Any, P : Persistence<T, ID>> Storage<T, ID, P>.loadOrFail(id: ID): P {
    return load(id) ?: throw EmptyResultDataAccessException(1)
}
