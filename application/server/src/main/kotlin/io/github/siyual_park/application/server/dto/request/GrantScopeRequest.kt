package io.github.siyual_park.application.server.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.AssertTrue
import javax.validation.constraints.Size

data class GrantScopeRequest(
    val id: Long? = null,
    @Size(max = 64)
    val name: String? = null,
) {
    @ApiModelProperty(hidden = true)
    @JsonIgnore
    @AssertTrue
    fun isValid(): Boolean {
        return id != null || name != null
    }
}
