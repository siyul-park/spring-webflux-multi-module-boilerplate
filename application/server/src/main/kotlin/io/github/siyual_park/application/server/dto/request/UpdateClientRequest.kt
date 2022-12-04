package io.github.siyual_park.application.server.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import io.github.siyual_park.ulid.ULID
import jakarta.validation.constraints.Size
import java.net.URL
import java.util.Optional

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UpdateClientRequest(
    val name: Optional<@Size(min = 3, max = 64) String>? = null,
    val origins: Optional<Collection<@Size(max = 2048) URL>>? = null,
    val scope: Optional<Collection<ULID>>? = null
)
