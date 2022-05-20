package io.github.siyual_park.application.server.handler

import com.google.common.base.CaseFormat
import io.github.siyual_park.application.server.dto.response.ErrorInfo
import io.github.siyual_park.json.bind.DataBufferWriter
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
@Order(-2)
class GlobalErrorWebExceptionHandler(
    private val dataBufferWriter: DataBufferWriter
) : ErrorWebExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalErrorWebExceptionHandler::class.java)

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        return mono {
            val statusCode = getStatus(ex) ?: HttpStatus.INTERNAL_SERVER_ERROR
            val cause = getCause(ex)
            val reason = getReason(ex)

            val error = if (statusCode.is5xxServerError) {
                logger.error(ex.message, ex)
                ErrorInfo(error = "internal_server_error", description = null)
            } else {
                logger.warn(ex.message, ex)
                ErrorInfo(error = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, cause.javaClass.simpleName), description = reason)
            }

            if (exchange.response.isCommitted) {
                throw ex
            }

            exchange.response.statusCode = statusCode
            dataBufferWriter.write(exchange.response, error).awaitSingleOrNull()
        }
    }

    private fun getStatus(ex: Throwable): HttpStatus? {
        return if (ex is ResponseStatusException) {
            ex.status
        } else {
            ex.cause?.let { getStatus(it) }
        }
    }

    private fun getReason(ex: Throwable): String? {
        return if (ex is ResponseStatusException) {
            ex.reason ?: ex.message
        } else {
            ex.message
        }
    }

    private fun getCause(ex: Throwable): Throwable {
        return ex.cause?.let { getCause(it) } ?: ex
    }
}
