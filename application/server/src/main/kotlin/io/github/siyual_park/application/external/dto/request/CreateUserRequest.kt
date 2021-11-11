package io.github.siyual_park.application.external.dto.request

import javax.validation.constraints.Size

data class CreateUserRequest(
    @field:Size(min = 3, max = 20)
    val name: String,
    @field:Size(min = 8, max = 20)
    val password: String
)
