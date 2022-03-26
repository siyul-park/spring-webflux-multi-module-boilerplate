package io.github.siyual_park.application.server.handler

import io.github.siyual_park.application.server.dto.response.ErrorInfo
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Suppress("LeakingThis")
@Component
@Order(-2)
class GlobalErrorWebExceptionHandler(
    errorAttributes: ErrorAttributes,
    webProperties: WebProperties,
    applicationContext: ApplicationContext,
    configurer: ServerCodecConfigurer
) : AbstractErrorWebExceptionHandler(errorAttributes, webProperties.resources, applicationContext) {
    private val logger = LoggerFactory.getLogger(GlobalErrorWebExceptionHandler::class.java)

    init {
        setMessageWriters(configurer.writers)
    }

    override fun getRoutingFunction(errorAttributes: ErrorAttributes): RouterFunction<ServerResponse> {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse)
    }

    private fun renderErrorResponse(request: ServerRequest): Mono<ServerResponse> {
        val errorAttributes = getErrorAttributes(request, ErrorAttributeOptions.defaults())
        val status = errorAttributes["status"] as Int
        val path = errorAttributes["path"] as String
        val error = errorAttributes["error"] as String
        val throwable = getError(request)

        return ServerResponse.status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                BodyInserters.fromValue(
                    if (isInternalError(status)) {
                        logger.error(throwable.message, error)
                        ErrorInfo(path = path, error = error, description = null)
                    } else {
                        logger.warn(throwable.message, error)
                        ErrorInfo(path = path, error = error, description = throwable.message)
                    }
                )
            )
    }

    private fun isInternalError(status: Int): Boolean {
        return status in 500..599
    }
}
