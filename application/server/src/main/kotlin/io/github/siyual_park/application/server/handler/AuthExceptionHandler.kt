package io.github.siyual_park.application.server.handler

import io.github.siyual_park.auth.exception.AuthException
import io.github.siyual_park.auth.exception.RequiredPermissionException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class AuthExceptionHandler {

    @ExceptionHandler(RequiredPermissionException::class)
    fun handle(exception: RequiredPermissionException) {
        throw ResponseStatusException(HttpStatus.FORBIDDEN, exception.message, exception)
    }

    @ExceptionHandler(UnsupportedOperationException::class)
    fun handle(exception: UnsupportedOperationException) {
        throw ResponseStatusException(HttpStatus.FORBIDDEN, exception.message, exception)
    }

    @ExceptionHandler(AuthException::class)
    fun handle(exception: AuthException) {
        throw ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.message, exception)
    }
}
