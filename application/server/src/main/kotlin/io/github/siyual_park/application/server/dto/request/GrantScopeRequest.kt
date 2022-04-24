package io.github.siyual_park.application.server.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.siyual_park.ulid.ULID
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.AssertTrue
import javax.validation.constraints.Size

data class GrantScopeRequest(
    val id: ULID? = null,
    @Size(max = 64)
    val name: String? = null,
) {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @JsonIgnore
    @AssertTrue
    fun isValid(): Boolean {
        return id != null || name != null
    }
}
