package io.github.siyual_park.application.external.handler

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import io.github.siyual_park.application.external.exception.BadRequestException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class VilificationExceptionHandler {

    @ExceptionHandler(MissingKotlinParameterException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handle(exception: MissingKotlinParameterException) {
        throw BadRequestException(exception.message)
    }
}
