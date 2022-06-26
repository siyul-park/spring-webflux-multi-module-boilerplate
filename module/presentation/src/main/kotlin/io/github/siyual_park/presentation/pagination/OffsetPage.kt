package io.github.siyual_park.presentation.pagination

data class OffsetPage<T>(
    val data: Collection<T>,
    val total: Long?,
    val perPage: Int,
    val page: Int?
)

inline fun <T, U> OffsetPage<T>.map(
    func: (Collection<T>) -> Collection<U>
) = OffsetPage(
    data = func(data),
    total = total,
    perPage = perPage,
    page = page
)
