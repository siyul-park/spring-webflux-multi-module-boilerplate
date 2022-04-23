package io.github.siyual_park.application.server.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.Optional
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UpdateUserCredentialRequest(
    val password: Optional<@Size(min = 8, max = 20) String>? = null,
)
