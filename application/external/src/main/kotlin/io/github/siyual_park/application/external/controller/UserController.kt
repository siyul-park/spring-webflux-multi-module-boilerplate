package io.github.siyual_park.application.external.controller

import io.github.siyual_park.application.external.dto.request.CreateUserRequest
import io.github.siyual_park.application.external.dto.response.CreateUserResponse
import io.github.siyual_park.application.external.dto.response.ReadUserResponse
import io.github.siyual_park.mapper.MapperManager
import io.github.siyual_park.mapper.map
import io.github.siyual_park.user.domain.CreateUserPayload
import io.github.siyual_park.user.domain.UserFactory
import io.github.siyual_park.user.domain.UserFinder
import io.github.siyual_park.user.domain.UserRemover
import io.github.siyual_park.user.domain.auth.UserPrincipal
import io.github.siyual_park.user.domain.auth.UserPrincipalExchanger
import io.swagger.annotations.Api
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Api("user")
@RestController
@RequestMapping("/users")
class UserController(
    private val userFactory: UserFactory,
    private val userPrincipalExchanger: UserPrincipalExchanger,
    private val userRemover: UserRemover,
    private val userFinder: UserFinder,
    private val mapperManager: MapperManager
) {

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody request: CreateUserRequest): CreateUserResponse {
        val payload: CreateUserPayload = mapperManager.map(request)
        val user = userFactory.create(payload)
        return mapperManager.map(user)
    }

    @GetMapping("/self")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(null, 'user:read.self')")
    suspend fun readSelf(@AuthenticationPrincipal principal: UserPrincipal): ReadUserResponse {
        val user = userPrincipalExchanger.exchange(principal)
        return mapperManager.map(user)
    }

    @DeleteMapping("/self")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(null, 'user:remove.self')")
    suspend fun deleteSelf(@AuthenticationPrincipal principal: UserPrincipal) {
        val user = userPrincipalExchanger.exchange(principal)
        userRemover.remove(user)
    }

    @DeleteMapping("/{user-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(null, 'user:remove')")
    suspend fun delete(@PathVariable("user-id") userId: Long) {
        val user = userFinder.findByIdOrFail(userId)
        userRemover.remove(user)
    }
}
