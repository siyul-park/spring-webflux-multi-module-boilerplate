package io.github.siyual_park.search.pagination

interface OffsetPaginator<T> {
    suspend fun paginate(query: OffsetPageQuery): OffsetPage<T>
}

suspend inline fun <T> OffsetPaginator<T>.forEach(perPage: Int, callBack: (Collection<T>) -> Unit) {
    var currentPage = 0

    while (true) {
        val page = this.paginate(
            OffsetPageQuery(
                perPage = perPage,
                page = currentPage
            )
        )

        callBack(page.data)

        if (page.data.size != perPage) {
            break
        }

        currentPage += 1
    }
}
