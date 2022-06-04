package io.github.siyual_park.auth.domain

import com.github.javafaker.Faker
import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException

class AuthenticationConverterTest : CoroutineTestHelper() {
    private val faker = Faker()

    private val authenticationConverter = AuthenticationConverter()

    @Test
    fun convertSuccess() = blocking {
        val request = MockServerHttpRequest.get("/")

        val type = faker.random().hex()
        val credentials = faker.random().hex()

        val headers = HttpHeaders().also {
            it[HttpHeaders.AUTHORIZATION] = "$type $credentials"
        }
        request.headers(headers)

        val exchange = MockServerWebExchange.from(request)

        val authentication = authenticationConverter.convert(exchange).awaitSingle()
        assertEquals(type, authentication.name)
        assertEquals(credentials, authentication.credentials)
    }

    @Test
    fun convertFail() = blocking {
        val request = MockServerHttpRequest.get("/")
        val exchange = MockServerWebExchange.from(request)

        val authentication = authenticationConverter.convert(exchange).awaitSingleOrNull()
        assertNull(authentication)

        val headers = HttpHeaders().also {
            it[HttpHeaders.AUTHORIZATION] = faker.random().hex()
        }
        request.headers(headers)

        assertThrows<AuthenticationCredentialsNotFoundException> { authenticationConverter.convert(exchange).awaitSingleOrNull() }
    }
}
