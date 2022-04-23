package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.response.UserContactInfo
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.map
import io.github.siyual_park.persistence.loadOrFail
import io.github.siyual_park.ulid.ULID
import io.github.siyual_park.user.domain.UserStorage
import io.swagger.annotations.Api
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Api(tags = ["user"])
@RestController
@RequestMapping("/users/{user-id}/contact")
class UserContactController(
    private val userStorage: UserStorage,
    private val mapperContext: MapperContext
) {
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission({null, #userId}, {'users.contact:read', 'users[self].contact:read'})")
    suspend fun read(
        @PathVariable("user-id") userId: ULID
    ): UserContactInfo {
        val user = userStorage.loadOrFail(userId)
        val contact = user.getContact()

        return mapperContext.map(contact)
    }
}
