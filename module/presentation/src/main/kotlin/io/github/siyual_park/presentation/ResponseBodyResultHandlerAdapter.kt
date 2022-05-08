package io.github.siyual_park.presentation

import kotlinx.coroutines.reactor.mono
import org.springframework.http.codec.HttpMessageWriter
import org.springframework.web.reactive.HandlerResult
import org.springframework.web.reactive.accept.RequestedContentTypeResolver
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

open class ResponseBodyResultHandlerAdapter<T : Any>(
    writers: List<HttpMessageWriter<*>>,
    resolver: RequestedContentTypeResolver,
    private val presenter: Presenter<T>
) : ResponseBodyResultHandler(writers, resolver) {
    override fun supports(result: HandlerResult): Boolean {
        return result.returnType.rawClass == presenter.type.java
    }

    override fun handleResult(exchange: ServerWebExchange, result: HandlerResult): Mono<Void> {
        return mono { presenter.present(exchange, result) }
            .flatMap { Mono.empty() }
    }
}
