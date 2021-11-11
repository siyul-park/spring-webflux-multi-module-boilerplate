package io.github.siyual_park.application.service.dto.request

import io.github.siyual_park.client.entity.ClientType
import javax.validation.constraints.Size

data class CreateClientRequest(
    @field:Size(min = 3, max = 20)
    val name: String,
    val type: ClientType,
)
