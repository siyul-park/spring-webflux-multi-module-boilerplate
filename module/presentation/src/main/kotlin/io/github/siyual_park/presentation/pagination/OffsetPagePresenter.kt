package io.github.siyual_park.presentation.pagination

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.siyual_park.presentation.Presenter
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.HandlerResult
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Suppress("UNCHECKED_CAST")
@Component
class OffsetPagePresenter(
    private val objectMapper: ObjectMapper
) : Presenter<OffsetPage<*>> {
    override val type = OffsetPage::class

    override suspend fun present(exchange: ServerWebExchange, result: HandlerResult) {
        val returnValue = (result.returnValue as Mono<OffsetPage<*>>).awaitSingleOrNull()
        if (returnValue != null) {
            val headers = exchange.response.headers
            val additional = mutableListOf<String>()

            if (returnValue.total != null) {
                headers["Total-Count"] = returnValue.total.toString()
                additional.add("Total-Count")
            }
            if (returnValue.page != null) {
                headers["Page"] = returnValue.page.toString()
                additional.add("Page")
            }
            headers["Per-Page"] = returnValue.perPage.toString()
            additional.add("Per-Page")

            headers[HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS] = additional.joinToString(", ")
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

            val response = exchange.response
            val dataBuffer = response.bufferFactory().wrap(objectMapper.writeValueAsBytes(returnValue.data))
            response.writeWith(Mono.just(dataBuffer)).awaitSingleOrNull()
        }
    }
}
