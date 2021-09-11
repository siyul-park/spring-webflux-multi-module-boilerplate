package io.github.siyual_park.application.external.controller

import io.github.siyual_park.application.external.dto.request.CreateUserRequest
import io.github.siyual_park.auth.domain.CreateUserPayload
import io.github.siyual_park.auth.domain.UserCreateExecutor
import io.github.siyual_park.auth.entity.User
import io.swagger.annotations.Api
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Api("user")
@RestController
@RequestMapping("/users")
class UserController(
    private val userCreateExecutor: UserCreateExecutor
) {

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody request: CreateUserRequest): User {
        val payload = CreateUserPayload(
            username = request.name,
            password = request.password
        )

        return userCreateExecutor.execute(payload)
    }
}
