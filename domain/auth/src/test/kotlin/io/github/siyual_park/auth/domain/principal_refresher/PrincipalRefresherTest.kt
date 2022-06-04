package io.github.siyual_park.auth.domain.principal_refresher

import com.github.javafaker.Faker
import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.data.test.DataTestHelper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PrincipalRefresherTest : DataTestHelper() {
    internal class TestPrincipal(
        override val id: String,
        override var scope: Set<ScopeToken>
    ) : Principal

    internal class TestPrincipalRefreshStrategy(
        private val principal: TestPrincipal
    ) : PrincipalRefreshStrategy<TestPrincipal> {
        override suspend fun refresh(principal: TestPrincipal): TestPrincipal {
            return this.principal
        }
    }

    private val faker = Faker()
    private val principalRefresher = PrincipalRefresher()

    @Test
    fun refresh() = blocking {
        val principal1 = TestPrincipal(faker.name().username(), emptySet())
        val principal2 = TestPrincipal(faker.name().username(), emptySet())

        principalRefresher.register(TestPrincipal::class, TestPrincipalRefreshStrategy(principal2))

        val principal3 = principalRefresher.refresh(principal1)

        assertEquals(principal2, principal3)
    }
}
