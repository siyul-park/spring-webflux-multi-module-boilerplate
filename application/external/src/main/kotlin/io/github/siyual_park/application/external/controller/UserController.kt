package io.github.siyual_park.application.external.controller

import io.github.siyual_park.application.external.dto.request.CreateUserRequest
import io.github.siyual_park.application.external.dto.response.CreateUserResponse
import io.github.siyual_park.auth.domain.CreateUserPayload
import io.github.siyual_park.auth.domain.UserCreateExecutor
import io.github.siyual_park.mapper.MapperManager
import io.github.siyual_park.mapper.map
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
    private val userCreateExecutor: UserCreateExecutor,
    private val mapperManager: MapperManager
) {

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody request: CreateUserRequest): CreateUserResponse {
        val payload: CreateUserPayload = mapperManager.map(request)
        val user = userCreateExecutor.execute(payload)
        return mapperManager.map(user)
    }
}
