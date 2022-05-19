package io.github.siyual_park.application.server.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import io.github.siyual_park.ulid.ULID
import java.util.Optional
import javax.validation.constraints.Email
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UpdateUserRequest(
    val name: Optional<@Size(min = 3, max = 20) String>? = null,
    val email: Optional<@Email @Size(min = 8, max = 64) String>? = null,
    val password: Optional<@Size(min = 8, max = 20) String>? = null,
    val scope: Optional<Collection<ULID>>? = null
)
