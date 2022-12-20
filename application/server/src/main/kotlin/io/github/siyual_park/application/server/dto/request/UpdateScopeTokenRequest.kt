package io.github.siyual_park.application.server.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import io.github.siyual_park.ulid.ULID
import jakarta.validation.constraints.Size
import java.util.Optional

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UpdateScopeTokenRequest(
    val name: Optional<@Size(min = 3, max = 64) String>? = null,
    val description: Optional<@Size(min = 3, max = 128) String>? = null,
    val children: Optional<Collection<ULID>>? = null
)
