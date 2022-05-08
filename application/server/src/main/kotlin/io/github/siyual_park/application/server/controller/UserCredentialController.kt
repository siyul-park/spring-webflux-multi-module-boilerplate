package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.request.UpdateUserCredentialRequest
import io.github.siyual_park.application.server.dto.response.UserCredentialInfo
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.map
import io.github.siyual_park.persistence.loadOrFail
import io.github.siyual_park.ulid.ULID
import io.github.siyual_park.user.domain.UserStorage
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
import javax.validation.ValidationException

@Tag(name = "user")
@RestController
@RequestMapping("/users/{user-id}/credential")
class UserCredentialController(
    private val userStorage: UserStorage,
    private val mapperContext: MapperContext
) {

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @PatchMapping("")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission({null, #userId}, {'users.credential:update', 'users[self].credential:update'})")
    suspend fun update(
        @PathVariable("user-id") userId: ULID,
        @Valid @RequestBody request: UpdateUserCredentialRequest
    ): UserCredentialInfo {
        val user = userStorage.loadOrFail(userId)
        val credential = user.getCredential()
        request.password?.let {
            credential.setPassword(it.orElseThrow { throw ValidationException("password is cannot be null") })
            credential.sync()
        }

        return mapperContext.map(credential)
    }
}
