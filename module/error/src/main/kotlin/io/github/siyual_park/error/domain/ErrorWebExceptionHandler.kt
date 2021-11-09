package io.github.siyual_park.error.domain

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.result.view.ViewResolver
import reactor.core.publisher.Mono

@Component
@Order(-2)
class ErrorWebExceptionHandler(
    resourceProperties: WebProperties.Resources,
    applicationContext: ApplicationContext,
    viewResolversProvider: ObjectProvider<List<ViewResolver>>,
    serverCodecConfigurer: ServerCodecConfigurer,
) : AbstractErrorWebExceptionHandler(DefaultErrorAttributes(), resourceProperties, applicationContext) {
    private val logger = LoggerFactory.getLogger(ErrorWebExceptionHandler::class.java)

    init {
        setViewResolvers(viewResolversProvider.getIfAvailable { emptyList() })
        setMessageWriters(serverCodecConfigurer.writers)
        setMessageReaders(serverCodecConfigurer.readers)
    }

    override fun getRoutingFunction(errorAttributes: ErrorAttributes) = RouterFunctions.route(
        RequestPredicates.all()
    ) { request -> handleErrorRequest(request) }

    private fun handleErrorRequest(request: ServerRequest): Mono<ServerResponse> {
        val errorAttributes = getErrorAttributes(
            request,
            ErrorAttributeOptions.defaults()
        )
        val status = errorAttributes["status"] as Int
        val path = errorAttributes["path"] as String
        val error = getError(request)

        return ServerResponse.status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                BodyInserters.fromValue(
                    if (isInternalError(status)) {
                        logger.error(error.message, error)

                        ErrorInfo(
                            path = path,
                            error = errorAttributes["error"] as String,
                            description = null
                        )
                    } else {
                        logger.warn(error.message, error)

                        ErrorInfo(
                            path = path,
                            error = errorAttributes["error"] as String,
                            description = error.message
                        )
                    }
                )
            )
    }

    private fun isInternalError(status: Int): Boolean {
        return 500 <= status && status < 600
    }
}
