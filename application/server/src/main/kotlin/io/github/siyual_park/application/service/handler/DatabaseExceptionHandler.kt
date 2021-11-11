package io.github.siyual_park.application.service.handler

import io.github.siyual_park.application.service.exception.ConflictException
import io.github.siyual_park.application.service.exception.NotFoundException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class DatabaseExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handle(exception: DataIntegrityViolationException) {
        throw ConflictException(exception.message)
    }

    @ExceptionHandler(EmptyResultDataAccessException::class)
    fun handle(exception: EmptyResultDataAccessException) {
        throw NotFoundException(exception.message)
    }
}
