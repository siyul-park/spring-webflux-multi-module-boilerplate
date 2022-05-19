package io.github.siyual_park.application.server.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import io.github.siyual_park.ulid.ULID
import java.util.Optional
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UpdateScopeTokenRequest(
    val name: Optional<@Size(min = 3, max = 20) String>? = null,
    val description: Optional<@Size(min = 3, max = 128) String>? = null,
    val children: Optional<Collection<ULID>>? = null
)
