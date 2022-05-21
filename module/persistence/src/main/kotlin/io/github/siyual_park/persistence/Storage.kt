package io.github.siyual_park.persistence

import kotlinx.coroutines.flow.Flow
import org.springframework.dao.EmptyResultDataAccessException

interface Storage<T : Any, ID : Any> {
    suspend fun load(id: ID): T?
    fun load(ids: Iterable<ID>): Flow<T>

    suspend fun count(): Long
}

suspend inline fun <T : Any, ID : Any> Storage<T, ID>.loadOrFail(id: ID): T {
    return load(id) ?: throw EmptyResultDataAccessException(1)
}
