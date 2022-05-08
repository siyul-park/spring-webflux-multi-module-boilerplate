package io.github.siyual_park.application.server.handler

import io.github.siyual_park.presentation.exception.InvalidRequestException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ResponseStatusException
import javax.validation.ValidationException

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class VilificationExceptionHandler {
    @ExceptionHandler(WebExchangeBindException::class)
    fun handle(exception: WebExchangeBindException) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, exception.message, exception)
    }

    @ExceptionHandler(InvalidRequestException::class)
    fun handle(exception: InvalidRequestException) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, exception.message, exception)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handle(exception: IllegalStateException) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, exception.message, exception)
    }

    @ExceptionHandler(ValidationException::class)
    fun handle(exception: ValidationException) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, exception.message, exception)
    }

    @ExceptionHandler(UnsupportedOperationException::class)
    fun handle(exception: UnsupportedOperationException) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, exception.message, exception)
    }
}
