package io.github.siyual_park.application.server.dto.request

import java.net.URL
import java.util.Optional
import javax.validation.constraints.Size

data class UpdateClientRequest(
    val name: Optional<@Size(min = 3, max = 20) String>? = null,
    val origin: Optional<@Size(max = 2048) URL>? = null
)
