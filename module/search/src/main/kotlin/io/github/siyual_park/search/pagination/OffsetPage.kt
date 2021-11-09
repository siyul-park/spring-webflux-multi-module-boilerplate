package io.github.siyual_park.search.pagination

class OffsetPage<T>(
    val data: Collection<T>,
    val total: Long,
    val perPage: Int,
    val page: Int
)
