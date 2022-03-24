package io.github.siyual_park.application.server.handler

import io.github.siyual_park.application.server.exception.ForbiddenException
import io.github.siyual_park.application.server.exception.UnauthorizedException
import io.github.siyual_park.auth.exception.AuthException
import io.github.siyual_park.auth.exception.RequiredPermissionException
import io.jsonwebtoken.JwtException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class AuthExceptionHandler {

    @ExceptionHandler(RequiredPermissionException::class)
    fun handle(exception: RequiredPermissionException) {
        throw ForbiddenException(exception.message)
    }

    @ExceptionHandler(UnsupportedOperationException::class)
    fun handle(exception: UnsupportedOperationException) {
        throw ForbiddenException(exception.message)
    }

    @ExceptionHandler(JwtException::class)
    fun handle(exception: JwtException) {
        throw UnauthorizedException(exception.message)
    }

    @ExceptionHandler(AuthException::class)
    fun handle(exception: AuthException) {
        throw UnauthorizedException(exception.message)
    }
}
