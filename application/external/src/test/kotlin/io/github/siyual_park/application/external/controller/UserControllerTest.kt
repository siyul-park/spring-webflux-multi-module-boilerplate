package io.github.siyual_park.application.external.controller

import io.github.siyual_park.application.external.factory.CreateUserPayloadFactory
import io.github.siyual_park.application.external.factory.CreateUserRequestFactory
import io.github.siyual_park.application.external.gateway.UserControllerGateway
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.spring.test.CoroutineTest
import io.github.siyual_park.spring.test.IntegrationTest
import io.github.siyual_park.user.domain.UserFactory
import io.github.siyual_park.user.domain.UserPrincipal
import io.github.siyual_park.user.domain.UserPrincipalExchanger
import kotlinx.coroutines.reactive.awaitSingle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

@IntegrationTest
class UserControllerTest @Autowired constructor(
    private val userControllerGateway: UserControllerGateway,
    private val userFactory: UserFactory,
    private val userPrincipalExchanger: UserPrincipalExchanger,
    private val scopeTokenRepository: ScopeTokenRepository
) : CoroutineTest() {
    private val createUserRequestFactory = CreateUserRequestFactory()
    private val createUserPayloadFactory = CreateUserPayloadFactory()

    @Test
    fun testCreateSuccess() = blocking {
        val request = createUserRequestFactory.create()
        val response = userControllerGateway.create(request)

        assertEquals(HttpStatus.CREATED, response.status)

        val user = response.responseBody.awaitSingle()

        assertNotNull(user.id)
        assertEquals(request.name, user.name)
        assertNotNull(user.createdAt)
        assertNotNull(user.updatedAt)
    }

    @Test
    fun testCreateFail() = blocking {
        val request = createUserRequestFactory.create()
        userControllerGateway.create(request)

        val response = userControllerGateway.create(request)
        assertEquals(HttpStatus.CONFLICT, response.status)
    }

    @Test
    fun testReadSelfSuccess() = blocking {
        val payload = createUserPayloadFactory.create()
        val user = userFactory.create(payload)
        val principal = userPrincipalExchanger.exchange(user)

        val response = userControllerGateway.readSelf(principal)

        assertEquals(HttpStatus.OK, response.status)

        val responseUser = response.responseBody.awaitSingle()

        assertEquals(user.id, responseUser.id)
        assertEquals(user.name, responseUser.name)
        assertNotNull(responseUser.createdAt)
        assertNotNull(responseUser.updatedAt)
    }

    @Test
    fun testReadSelfFail() = blocking {
        val payload = createUserPayloadFactory.create()
        val user = userFactory.create(payload)
        val principal = userPrincipalExchanger.exchange(user)

        val removeScope = scopeTokenRepository.findByNameOrFail("user:read.self")

        val response = userControllerGateway.readSelf(
            UserPrincipal(
                id = principal.id,
                scope = principal.scope.filter { it.id != removeScope.id }.toSet()
            )
        )

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }
}
