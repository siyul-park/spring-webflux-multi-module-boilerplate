package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.factory.CreateClientPayloadFactory
import io.github.siyual_park.application.server.factory.CreateUserPayloadFactory
import io.github.siyual_park.application.server.factory.CreateUserRequestFactory
import io.github.siyual_park.application.server.gateway.factory.UserControllerGatewayFactory
import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.client.domain.ClientFactory
import io.github.siyual_park.client.domain.auth.ClientPrincipalExchanger
import io.github.siyual_park.spring.test.CoroutineTest
import io.github.siyual_park.spring.test.IntegrationTest
import io.github.siyual_park.user.domain.UserFactory
import io.github.siyual_park.user.domain.UserScopeFinder
import io.github.siyual_park.user.domain.auth.UserPrincipal
import io.github.siyual_park.user.domain.auth.UserPrincipalExchanger
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.reactive.awaitSingle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

@IntegrationTest
class UserControllerTest @Autowired constructor(
    private val userControllerGatewayFactory: UserControllerGatewayFactory,
    private val userFactory: UserFactory,
    private val clientFactory: ClientFactory,
    private val userPrincipalExchanger: UserPrincipalExchanger,
    private val clientPrincipalExchanger: ClientPrincipalExchanger,
    private val userScopeFinder: UserScopeFinder,
    private val scopeTokenRepository: ScopeTokenRepository
) : CoroutineTest() {
    private val createUserRequestFactory = CreateUserRequestFactory()
    private val createUserPayloadFactory = CreateUserPayloadFactory()
    private val createClientPayloadFactory = CreateClientPayloadFactory()

    @Test
    fun testCreateSuccess() = blocking {
        val principal = createClientPayloadFactory.create()
            .let { clientFactory.create(it) }
            .let { clientPrincipalExchanger.exchange(it) }

        val userControllerGateway = userControllerGatewayFactory.create(principal)

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
        val principal = createClientPayloadFactory.create()
            .let { clientFactory.create(it) }
            .let { clientPrincipalExchanger.exchange(it) }

        val userControllerGateway = userControllerGatewayFactory.create(principal)

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

        val userControllerGateway = userControllerGatewayFactory.create(principal)

        val response = userControllerGateway.readSelf()

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

        val userControllerGateway = userControllerGatewayFactory.create(
            UserPrincipal(
                id = principal.id,
                scope = principal.scope.filter { it.id != removeScope.id }.toSet()
            )
        )

        val response = userControllerGateway.readSelf()

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun testDeleteSelfSuccess() = blocking {
        val payload = createUserPayloadFactory.create()
        val user = userFactory.create(payload)
        val principal = userPrincipalExchanger.exchange(user)

        val userControllerGateway = userControllerGatewayFactory.create(principal)

        val response = userControllerGateway.deleteSelf()

        assertEquals(HttpStatus.NO_CONTENT, response.status)

        val scope = userScopeFinder.findAllByUser(user).toSet()
        assertEquals(0, scope.size)
    }

    @Test
    fun testDeleterSelfFail() = blocking {
        val payload = createUserPayloadFactory.create()
        val user = userFactory.create(payload)
        val principal = userPrincipalExchanger.exchange(user)

        val removeScope = scopeTokenRepository.findByNameOrFail("user:delete.self")

        val userControllerGateway = userControllerGatewayFactory.create(
            UserPrincipal(
                id = principal.id,
                scope = principal.scope.filter { it.id != removeScope.id }.toSet()
            )
        )

        val response = userControllerGateway.deleteSelf()

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun testDeleterSuccess() = blocking {
        val payload = createUserPayloadFactory.create()
        val user = userFactory.create(payload)
        val principal = userPrincipalExchanger.exchange(user)

        val otherPayload = createUserPayloadFactory.create()
        val otherUser = userFactory.create(otherPayload)

        val removeScope = scopeTokenRepository.findByNameOrFail("user:delete")

        val scope = mutableSetOf<ScopeToken>()
        scope.add(removeScope)
        scope.addAll(principal.scope)

        val userControllerGateway = userControllerGatewayFactory.create(
            UserPrincipal(
                id = principal.id,
                scope = scope
            )
        )

        val response = userControllerGateway.delete(otherUser.id!!)

        assertEquals(HttpStatus.NO_CONTENT, response.status)
    }

    @Test
    fun testDeleterFail() = blocking {
        val payload = createUserPayloadFactory.create()
        val user = userFactory.create(payload)
        val principal = userPrincipalExchanger.exchange(user)

        val otherPayload = createUserPayloadFactory.create()
        val otherUser = userFactory.create(otherPayload)

        val userControllerGateway = userControllerGatewayFactory.create(principal)

        val response = userControllerGateway.delete(otherUser.id!!)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }
}
