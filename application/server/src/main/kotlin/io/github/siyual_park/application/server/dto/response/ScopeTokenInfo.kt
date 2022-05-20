package io.github.siyual_park.application.server.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import io.github.siyual_park.ulid.ULID
import java.time.Instant
import java.util.Optional

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ScopeTokenInfo(
    val id: Optional<ULID>?,
    val name: Optional<String>?,
    val description: Optional<String>?,
    val system: Optional<Boolean>?,
    val children: Optional<Collection<ScopeTokenInfo>>?,
    val createdAt: Optional<Instant>?,
    val updatedAt: Optional<Instant>?,
)
