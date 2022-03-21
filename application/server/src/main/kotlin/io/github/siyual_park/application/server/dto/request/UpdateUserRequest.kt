package io.github.siyual_park.application.server.dto.request

import java.util.Optional
import javax.validation.constraints.Size

data class UpdateUserRequest(
    var name: Optional<@Size(min = 3, max = 20) String>? = null,
)
