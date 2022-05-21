package io.github.siyual_park.data.cache

import org.springframework.data.domain.Sort

data class SelectQuery(
    val where: String?,
    val limit: Int? = null,
    val offset: Long? = null,
    val sort: Sort? = null
)
