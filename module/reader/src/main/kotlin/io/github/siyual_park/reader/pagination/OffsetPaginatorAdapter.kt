package io.github.siyual_park.reader.pagination

import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.query.CriteriaDefinition

class OffsetPaginatorAdapter<T : Any, ID : Any>(
    private val repository: R2DBCRepository<T, ID>,
    private val criteria: CriteriaDefinition? = null,
    private val sort: Sort? = null
) : OffsetPaginator<T> {

    override suspend fun paginate(query: OffsetPageQuery): OffsetPage<T> {
        val data = repository.findAll(
            criteria,
            limit = query.perPage,
            offset = (query.page * query.perPage).toLong(),
            sort = sort
        ).toList()

        val total = repository.count(criteria)

        return OffsetPage(
            data = data,
            total = total,
            perPage = query.perPage,
            page = query.page
        )
    }
}
