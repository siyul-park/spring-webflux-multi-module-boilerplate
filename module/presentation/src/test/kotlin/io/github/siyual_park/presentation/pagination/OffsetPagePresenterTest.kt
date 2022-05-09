package io.github.siyual_park.presentation.pagination

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.presentation.entity.Person
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.reactor.awaitSingle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.mock.http.server.reactive.MockServerHttpResponse
import org.springframework.web.reactive.HandlerResult
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Suppress("ReactiveStreamsUnusedPublisher")
class OffsetPagePresenterTest : CoroutineTestHelper() {
    private val objectMapper = ObjectMapper().registerModule(
        KotlinModule.Builder()
            .withReflectionCacheSize(512)
            .configure(KotlinFeature.NullToEmptyCollection, false)
            .configure(KotlinFeature.NullToEmptyMap, false)
            .configure(KotlinFeature.NullIsSameAsDefault, false)
            .configure(KotlinFeature.SingletonSupport, false)
            .configure(KotlinFeature.StrictNullChecks, false)
            .build()
    )
    private val offsetPagePresenter = OffsetPagePresenter(objectMapper)

    @Test
    fun present() = blocking {
        val exchange = mockk<ServerWebExchange>()
        val result = mockk<HandlerResult>()

        val serverResponse = MockServerHttpResponse()
        val person = Person("", 0)
        val page = OffsetPage(
            data = listOf(person),
            total = 1,
            perPage = 10,
            page = 0
        )

        every { exchange.response } returns serverResponse
        every { result.returnValue } returns Mono.just(page)

        offsetPagePresenter.present(exchange, result)

        val headers = serverResponse.headers
        assertEquals(listOf(page.total.toString()), headers["Total-Count"])
        assertEquals(listOf(page.page.toString()), headers["Page"])
        assertEquals(listOf(page.perPage.toString()), headers["Per-Page"])

        val body = serverResponse.bodyAsString.awaitSingle()
            .let { objectMapper.readValue(it, object : TypeReference<List<Person>>() {}) }
        assertEquals(1, body.size)
        assertEquals(person, body[0])
    }
}
