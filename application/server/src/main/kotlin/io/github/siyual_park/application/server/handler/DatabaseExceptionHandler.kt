package io.github.siyual_park.application.server.handler

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class DatabaseExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handle(exception: DataIntegrityViolationException) {
        throw ResponseStatusException(HttpStatus.CONFLICT, exception.message, exception)
    }

    @ExceptionHandler(EmptyResultDataAccessException::class)
    fun handle(exception: EmptyResultDataAccessException) {
        throw ResponseStatusException(HttpStatus.NOT_FOUND, exception.message, exception)
    }
}
