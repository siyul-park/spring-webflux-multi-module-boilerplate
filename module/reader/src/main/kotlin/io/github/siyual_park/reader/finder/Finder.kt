package io.github.siyual_park.reader.finder

import kotlinx.coroutines.flow.Flow
import org.springframework.dao.EmptyResultDataAccessException

interface Finder<T : Any, ID : Any> {
    suspend fun findById(id: ID): T?

    fun findAll(): Flow<T>

    fun findAllById(ids: Iterable<ID>): Flow<T>
}

suspend fun <T : Any, ID : Any> Finder<T, ID>.findByIdOrFail(id: ID): T {
    return findById(id) ?: throw EmptyResultDataAccessException(1)
}
