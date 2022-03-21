package io.github.siyual_park.search.pagination

data class OffsetPage<T>(
    val data: Collection<T>,
    val total: Long,
    val perPage: Int,
    val page: Int
) {
    fun <U> mapData(
        func: (Collection<T>) -> Collection<U>
    ) = OffsetPage(
        data = func(data),
        total = total,
        perPage = perPage,
        page = page
    )

    suspend fun <U> mapDataAsync(
        func: suspend (Collection<T>) -> Collection<U>
    ) = OffsetPage(
        data = func(data),
        total = total,
        perPage = perPage,
        page = page
    )
}
