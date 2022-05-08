package io.github.siyual_park.presentation

import org.springframework.web.reactive.HandlerResult
import org.springframework.web.server.ServerWebExchange
import kotlin.reflect.KClass

interface Presenter<T : Any> {
    val type: KClass<T>

    suspend fun present(exchange: ServerWebExchange, result: HandlerResult)
}
