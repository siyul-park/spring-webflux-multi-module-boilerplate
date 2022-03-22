package io.github.siyual_park.application.server.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.validation.constraints.AssertTrue

data class GrantScopeRequest(
    val id: Long? = null,
    val name: String? = null,
) {
    @JsonIgnore
    @AssertTrue
    fun isValid(): Boolean {
        return id != null || name != null
    }
}
