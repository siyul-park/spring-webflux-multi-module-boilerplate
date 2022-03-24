package io.github.siyual_park.application.server.dto.response

data class PrincipalInfo(
    val id: String,
    val type: String,
    val scope: Collection<ScopeTokenInfo>
)
