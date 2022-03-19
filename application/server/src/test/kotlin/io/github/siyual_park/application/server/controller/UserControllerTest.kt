package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.request.MutableUserData
import io.github.siyual_park.application.server.dummy.DummyCreateClientPayload
import io.github.siyual_park.application.server.dummy.DummyCreateUserPayload
import io.github.siyual_park.application.server.dummy.DummyCreateUserRequest
import io.github.siyual_park.application.server.dummy.RandomNameFactory
import io.github.siyual_park.application.server.gateway.GatewayAuthorization
import io.github.siyual_park.application.server.gateway.UserControllerGateway
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFinder
import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.auth.entity.ids
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.client.domain.ClientFactory
import io.github.siyual_park.client.domain.auth.ClientPrincipalExchanger
import io.github.siyual_park.spring.test.CoroutineTest
import io.github.siyual_park.spring.test.IntegrationTest
import io.github.siyual_park.user.domain.UserFactory
import io.github.siyual_park.user.domain.UserScopeFinder
import io.github.siyual_park.user.domain.auth.UserPrincipal
import io.github.siyual_park.user.domain.auth.UserPrincipalExchanger
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.reactive.awaitSingle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

@IntegrationTest
class UserControllerTest @Autowired constructor(
    private val gatewayAuthorization: GatewayAuthorization,
    private val userControllerGateway: UserControllerGateway,
    private val userFactory: UserFactory,
    private val clientFactory: ClientFactory,
    private val userPrincipalExchanger: UserPrincipalExchanger,
    private val clientPrincipalExchanger: ClientPrincipalExchanger,
    private val userScopeFinder: UserScopeFinder,
    private val scopeTokenFinder: ScopeTokenFinder,
    private val scopeTokenRepository: ScopeTokenRepository
) : CoroutineTest() {

    @Test
    fun testCreateSuccess() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }
            .let { clientPrincipalExchanger.exchange(it) }

        gatewayAuthorization.setPrincipal(principal)

        val request = DummyCreateUserRequest.create()
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
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }
            .let { clientPrincipalExchanger.exchange(it) }

        gatewayAuthorization.setPrincipal(principal)

        val request = DummyCreateUserRequest.create()
        userControllerGateway.create(request)

        val response = userControllerGateway.create(request)
        assertEquals(HttpStatus.CONFLICT, response.status)
    }

    @Test
    fun testReadSelfSuccess() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = userPrincipalExchanger.exchange(user)

        gatewayAuthorization.setPrincipal(principal)

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
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = userPrincipalExchanger.exchange(user)

        val removeScope = scopeTokenRepository.findByNameOrFail("users[self]:read")

        gatewayAuthorization.setPrincipal(
            UserPrincipal(
                id = principal.id,
                scope = scopeTokenFinder.findAllWithResolved(principal.scope.ids())
                    .filter { it.id != removeScope.id }
                    .toSet()
            )
        )

        val response = userControllerGateway.readSelf()

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun testUpdateSelfSuccess() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = userPrincipalExchanger.exchange(user)

        gatewayAuthorization.setPrincipal(principal)

        val request = MutableUserData(
            name = RandomNameFactory.create(10)
        )
        val response = userControllerGateway.updateSelf(request)

        assertEquals(HttpStatus.OK, response.status)

        val responseUser = response.responseBody.awaitSingle()

        assertEquals(user.id, responseUser.id)
        assertEquals(request.name, responseUser.name)
        assertNotNull(responseUser.createdAt)
        assertNotNull(responseUser.updatedAt)
    }

    @Test
    fun testDeleteSelfSuccess() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = userPrincipalExchanger.exchange(user)

        gatewayAuthorization.setPrincipal(principal)

        val response = userControllerGateway.deleteSelf()

        assertEquals(HttpStatus.NO_CONTENT, response.status)

        val scope = userScopeFinder.findAllWithResolvedByUser(user).toSet()
        assertEquals(0, scope.size)
    }

    @Test
    fun testDeleterSelfFail() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = userPrincipalExchanger.exchange(user)

        val removeScope = scopeTokenRepository.findByNameOrFail("users[self]:delete")

        gatewayAuthorization.setPrincipal(
            UserPrincipal(
                id = principal.id,
                scope = scopeTokenFinder.findAllWithResolved(principal.scope.ids())
                    .filter { it.id != removeScope.id }
                    .toSet()
            )
        )

        val response = userControllerGateway.deleteSelf()

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun testDeleteSuccess() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = userPrincipalExchanger.exchange(user)

        val otherPayload = DummyCreateUserPayload.create()
        val otherUser = userFactory.create(otherPayload)

        val removeScope = scopeTokenRepository.findByNameOrFail("users:delete")

        val scope = mutableSetOf<ScopeToken>()
        scope.add(removeScope)
        scope.addAll(principal.scope)

        gatewayAuthorization.setPrincipal(
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
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = userPrincipalExchanger.exchange(user)

        val otherPayload = DummyCreateUserPayload.create()
        val otherUser = userFactory.create(otherPayload)

        gatewayAuthorization.setPrincipal(principal)

        val response = userControllerGateway.delete(otherUser.id!!)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }
}
