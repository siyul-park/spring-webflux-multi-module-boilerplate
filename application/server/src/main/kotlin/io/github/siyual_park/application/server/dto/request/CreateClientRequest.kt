package io.github.siyual_park.application.server.dto.request

import io.github.siyual_park.client.entity.ClientType
import jakarta.validation.constraints.Size
import java.net.URL

data class CreateClientRequest(
    @field:Size(min = 3, max = 64)
    val name: String,
    val type: ClientType,
    val origins: Collection<@Size(max = 2048) URL>,
)
