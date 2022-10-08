package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.auth.migration.CreateScopeRelation
import io.github.siyual_park.auth.migration.CreateScopeToken
import io.github.siyual_park.auth.repository.ScopeRelationDataRepository
import io.github.siyual_park.auth.repository.ScopeTokenDataRepository
import io.github.siyual_park.data.test.DataTestHelper
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.EmptyResultDataAccessException

class ScopeTokenStorageTest : DataTestHelper() {
    init {
        migrationManager
            .register(CreateScopeToken(entityOperations))
            .register(CreateScopeRelation(entityOperations))
    }

    private val scopeRelationDataRepository = ScopeRelationDataRepository(entityOperations)
    private val scopeTokenDataRepository = ScopeTokenDataRepository(entityOperations)

    private val scopeTokenMapper = ScopeTokenMapper(scopeTokenDataRepository, scopeRelationDataRepository)
    private val scopeTokenFactory = ScopeTokenFactory(scopeTokenDataRepository, scopeTokenMapper)
    private val scopeTokenStorage = ScopeTokenStorage(scopeTokenDataRepository, scopeTokenMapper)

    @Test
    fun load() = blocking {
        val origin = MockCreateScopeTokenPayloadFactory.create()
            .let { scopeTokenFactory.create(it) }

        val found = scopeTokenStorage.load(origin.name)

        assertEquals(origin, found)
    }

    @Test
    fun loadOrFail() = blocking {
        val origin = MockCreateScopeTokenPayloadFactory.create()
            .let { scopeTokenFactory.create(it) }
        val otherName = MockScopeNameFactory.create()

        val found = scopeTokenStorage.loadOrFail(origin.name)
        assertEquals(origin, found)
        assertThrows<EmptyResultDataAccessException> { scopeTokenStorage.loadOrFail(otherName) }
    }

    @Test
    fun loadMany() = blocking {
        val origin1 = MockCreateScopeTokenPayloadFactory.create()
            .let { scopeTokenFactory.create(it) }
        val origin2 = MockCreateScopeTokenPayloadFactory.create()
            .let { scopeTokenFactory.create(it) }

        val found = scopeTokenStorage.load(listOf(origin1.name, origin2.name)).toList()

        assertEquals(2, found.size)
        assertTrue(found.contains(origin1))
        assertTrue(found.contains(origin2))
    }
}
