package io.github.siyual_park.search.pagination

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.siyual_park.viewer.Presenter
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.HandlerResult
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class OffsetPagePresenter(
    private val objectMapper: ObjectMapper
) : Presenter<OffsetPage<*>> {
    override val type = OffsetPage::class

    override suspend fun present(exchange: ServerWebExchange, result: HandlerResult) {
        val returnValue = (result.returnValue as Mono<OffsetPage<*>>).awaitSingleOrNull()
        if (returnValue != null) {
            val headers = exchange.response.headers

            headers["Total-Count"] = returnValue.total.toString()
            headers["Page"] = returnValue.page.toString()
            headers["Per-Page"] = returnValue.perPage.toString()
            headers[HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS] = "Total-Count, Page, Per-Page"

            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

            val response = exchange.response

            val dataBuffer = response.bufferFactory().wrap(objectMapper.writeValueAsBytes(returnValue.data))
            response.writeWith(Mono.just(dataBuffer)).awaitSingleOrNull()
        }
    }
}
