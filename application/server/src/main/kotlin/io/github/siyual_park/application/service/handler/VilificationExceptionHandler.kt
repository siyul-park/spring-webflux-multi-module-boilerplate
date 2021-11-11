package io.github.siyual_park.application.service.handler

import io.github.siyual_park.application.service.exception.BadRequestException
import io.github.siyual_park.reader.exception.InvalidRequestException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class VilificationExceptionHandler {
    @ExceptionHandler(WebExchangeBindException::class)
    fun handle(exception: WebExchangeBindException) {
        throw BadRequestException(exception.message)
    }

    @ExceptionHandler(InvalidRequestException::class)
    fun handle(exception: InvalidRequestException) {
        throw BadRequestException(exception.message)
    }
}
