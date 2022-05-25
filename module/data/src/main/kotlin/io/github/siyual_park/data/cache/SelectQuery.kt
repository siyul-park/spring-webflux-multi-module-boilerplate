package io.github.siyual_park.data.cache

import io.github.siyual_park.data.criteria.Criteria
import org.springframework.data.domain.Sort

data class SelectQuery(
    val where: Criteria?,
    val limit: Int? = null,
    val offset: Long? = null,
    val sort: Sort? = null
)
