package io.github.siyual_park.search.pagination

import io.github.siyual_park.persistence.Persistence
import io.github.siyual_park.persistence.R2DBCStorage
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.query.CriteriaDefinition

class OffsetPaginator<T : Any, ID : Any, P : Persistence<T, ID>>(
    private val storage: R2DBCStorage<T, ID, P>,
) {
    suspend fun paginate(
        criteria: CriteriaDefinition? = null,
        sort: Sort? = null,
        page: Int = 0,
        perPage: Int = 15
    ): OffsetPage<P> {
        val data = storage.load(
            criteria,
            limit = perPage,
            offset = (page * perPage).toLong(),
            sort = sort
        ).toList()

        val total = storage.count(criteria)

        return OffsetPage(
            data = data,
            total = total,
            perPage = perPage,
            page = page
        )
    }
}
