package io.github.siyual_park.application.external.controller

import io.swagger.annotations.Api
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Api(tags = ["utility"])
@RestController
class RootController {

    @GetMapping("/")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun root() {
    }
}
