package io.github.siyual_park.application.server.dto.request

import io.github.siyual_park.client.entity.ClientType
import java.net.URL
import javax.validation.constraints.Size

data class CreateClientRequest(
    @field:Size(min = 3, max = 20)
    val name: String,
    val type: ClientType,
    @field:Size(max = 2048)
    val origin: URL,
)
