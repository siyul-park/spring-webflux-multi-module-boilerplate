package io.github.siyual_park.search.pagination

import io.github.siyual_park.persistence.R2DBCStorage
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.query.CriteriaDefinition

class OffsetPaginator<T : Any, ID : Any>(
    private val storage: R2DBCStorage<T, ID>,
) {
    suspend fun paginate(
        criteria: CriteriaDefinition? = null,
        sort: Sort? = null,
        page: Int?,
        perPage: Int?
    ): OffsetPage<T> {
        val finalPage = page ?: 0
        val finalPerPage = (perPage ?: 15).coerceAtMost(150)

        val data = storage.load(
            criteria,
            limit = finalPerPage,
            offset = (finalPage * finalPerPage).toLong(),
            sort = sort
        ).toList()

        val total = if (page == null) {
            null
        } else {
            storage.count(criteria)
        }

        return OffsetPage(
            data = data,
            total = total,
            perPage = finalPerPage,
            page = page
        )
    }
}
