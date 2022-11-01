package io.github.siyual_park.application.server.dto.request

import javax.validation.constraints.Email
import javax.validation.constraints.Size

data class CreateUserRequest(
    @field:Size(min = 3, max = 64)
    val name: String,
    @field:Email
    @field:Size(min = 8, max = 128)
    val email: String,
    @field:Size(min = 8, max = 20)
    val password: String
)
