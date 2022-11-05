package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.auth.migration.CreateScopeRelation
import io.github.siyual_park.auth.migration.CreateScopeToken
import io.github.siyual_park.auth.repository.ScopeRelationEntityRepository
import io.github.siyual_park.auth.repository.ScopeTokenEntityRepository
import io.github.siyual_park.data.test.DataTestHelper
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.EmptyResultDataAccessException

class ScopeTokenStorageTest : DataTestHelper() {
    init {
        migrationManager
            .register(CreateScopeToken(entityOperations))
            .register(CreateScopeRelation(entityOperations))
    }

    private val scopeRelationEntityRepository = ScopeRelationEntityRepository(entityOperations)
    private val scopeTokenEntityRepository = ScopeTokenEntityRepository(entityOperations)

    private val scopeTokenMapper = ScopeTokenMapper(scopeTokenEntityRepository, scopeRelationEntityRepository)
    private val scopeTokenStorage = ScopeTokenStorage(scopeTokenEntityRepository, scopeTokenMapper)

    @Test
    fun upsert() = blocking {
        val payload = MockCreateScopeTokenPayloadFactory.create()
        val name = payload.name
        val description = payload.description

        val scopeToken1 = scopeTokenStorage.upsert(name)
        assertEquals(name, scopeToken1.name)

        val scopeToken2 = scopeTokenStorage.upsert(name)
        assertEquals(name, scopeToken2.name)
        assertEquals(scopeToken1.id, scopeToken2.id)

        val scopeToken3 = scopeTokenStorage.upsert(CreateScopeTokenPayload(name, description))
        assertEquals(name, scopeToken3.name)
        assertEquals(description, scopeToken3.description)
        assertEquals(scopeToken1.id, scopeToken3.id)
    }

    @Test
    fun save() = blocking {
        val payload = MockCreateScopeTokenPayloadFactory.create()

        val scopeToken = scopeTokenStorage.save(payload)
        assertEquals(payload.name, scopeToken.name)
        assertEquals(payload.description, scopeToken.description)

        assertThrows<DataIntegrityViolationException> { scopeTokenStorage.save(payload) }
    }

    @Test
    fun load() = blocking {
        val origin = MockCreateScopeTokenPayloadFactory.create()
            .let { scopeTokenStorage.save(it) }

        val found = scopeTokenStorage.load(origin.name)

        assertEquals(origin, found)
    }

    @Test
    fun loadOrFail() = blocking {
        val origin = MockCreateScopeTokenPayloadFactory.create()
            .let { scopeTokenStorage.save(it) }
        val otherName = MockScopeNameFactory.create()

        val found = scopeTokenStorage.loadOrFail(origin.name)
        assertEquals(origin, found)
        assertThrows<EmptyResultDataAccessException> { scopeTokenStorage.loadOrFail(otherName) }
    }

    @Test
    fun loadMany() = blocking {
        val origin1 = MockCreateScopeTokenPayloadFactory.create()
            .let { scopeTokenStorage.save(it) }
        val origin2 = MockCreateScopeTokenPayloadFactory.create()
            .let { scopeTokenStorage.save(it) }

        val found = scopeTokenStorage.load(listOf(origin1.name, origin2.name)).toList()

        assertEquals(2, found.size)
        assertTrue(found.contains(origin1))
        assertTrue(found.contains(origin2))
    }
}
