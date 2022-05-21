package io.github.siyual_park.data.repository.r2dbc

import org.springframework.data.domain.Sort

data class SelectQuery(
    val where: String?,
    val limit: Int?,
    val offset: Long?,
    val sort: Sort?
)
