package io.github.siyual_park.application.server.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "utility")
@RestController
class RootController {

    @GetMapping("/")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun root() {
    }
}
