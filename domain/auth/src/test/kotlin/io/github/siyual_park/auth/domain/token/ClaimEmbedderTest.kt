package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.data.test.DataTestHelper
import io.github.siyual_park.ulid.ULID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClaimEmbedderTest : DataTestHelper() {
    internal class TestPrincipal(
        override val id: ULID,
        override var scope: Set<ScopeToken>
    ) : Principal

    internal class TestClaimEmbeddingStrategy : ClaimEmbeddingStrategy<TestPrincipal> {
        override val clazz = TestPrincipal::class

        override suspend fun embedding(principal: TestPrincipal): Map<String, Any> {
            return mapOf(
                "id" to principal.id
            )
        }
    }

    private val claimEmbedder = ClaimEmbedder()

    @Test
    fun embedding() = blocking {
        val principal = TestPrincipal(ULID.randomULID(), emptySet())

        claimEmbedder.register(TypeMatchClaimFilter(TestPrincipal::class), TestClaimEmbeddingStrategy())

        val claims = claimEmbedder.embedding(principal)

        assertEquals(mapOf("id" to principal.id), claims)
    }
}
