package io.github.siyual_park.application.server.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import io.github.siyual_park.ulid.ULID
import java.time.Instant
import java.util.Optional

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserInfo(
    val id: Optional<ULID>? = null,
    val name: Optional<String>? = null,
    val email: Optional<String>? = null,
    val scope: Optional<Collection<ScopeTokenInfo>>? = null,
    val createdAt: Optional<Instant>? = null,
    val updatedAt: Optional<Instant>? = null
)
