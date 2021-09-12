package io.github.siyual_park.application.external.handler

import io.github.siyual_park.application.external.exception.UnauthorizedException
import io.github.siyual_park.auth.exception.AuthException
import io.github.siyual_park.auth.exception.RequiredPermissionException
import io.jsonwebtoken.JwtException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class AuthExceptionHandler {

    @ExceptionHandler(RequiredPermissionException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handle(exception: RequiredPermissionException) {
        throw UnauthorizedException(exception.message)
    }

    @ExceptionHandler(JwtException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handle(exception: JwtException) {
        throw UnauthorizedException(exception.message)
    }

    @ExceptionHandler(AuthException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handle(exception: AuthException) {
        throw UnauthorizedException(exception.message)
    }
}
