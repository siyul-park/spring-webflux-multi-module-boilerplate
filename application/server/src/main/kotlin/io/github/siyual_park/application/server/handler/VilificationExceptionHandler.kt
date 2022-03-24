package io.github.siyual_park.application.server.handler

import io.github.siyual_park.application.server.exception.BadRequestException
import io.github.siyual_park.search.exception.InvalidRequestException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import javax.validation.ValidationException

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

    @ExceptionHandler(IllegalStateException::class)
    fun handle(exception: IllegalStateException) {
        throw BadRequestException(exception.message)
    }

    @ExceptionHandler(ValidationException::class)
    fun handle(exception: ValidationException) {
        throw BadRequestException(exception.message)
    }
}
