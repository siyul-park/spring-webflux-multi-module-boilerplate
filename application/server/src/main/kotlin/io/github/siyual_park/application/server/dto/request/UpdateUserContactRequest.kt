package io.github.siyual_park.application.server.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.Optional
import javax.validation.constraints.Email
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UpdateUserContactRequest(
    val email: Optional<@Email @Size(min = 8, max = 64) String>? = null
)
