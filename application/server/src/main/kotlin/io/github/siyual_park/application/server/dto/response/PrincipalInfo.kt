package io.github.siyual_park.application.server.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.Optional

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PrincipalInfo(
    val id: Optional<String>?,
    val type: Optional<String>?,
    val scope: Optional<Collection<ScopeTokenInfo>>?
)
