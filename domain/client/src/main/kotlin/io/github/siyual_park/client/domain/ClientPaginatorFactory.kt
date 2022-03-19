package io.github.siyual_park.client.domain

import io.github.siyual_park.client.entity.Client
import io.github.siyual_park.client.repository.ClientRepository
import io.github.siyual_park.data.expansion.columnName
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.reader.pagination.OffsetPaginator
import io.github.siyual_park.reader.pagination.OffsetPaginatorAdapter
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.CriteriaDefinition
import org.springframework.stereotype.Component

@Component
class ClientPaginatorFactory(
    private val clientRepository: ClientRepository,
) {
    private val filter = where(Client::deletedAt).isNull

    fun create(
        criteria: CriteriaDefinition? = null,
        sort: Sort? = null
    ): OffsetPaginator<Client> {
        return OffsetPaginatorAdapter(
            clientRepository,
            sort = sort ?: Sort.by(Sort.Direction.DESC, columnName(Client::createdAt)),
            criteria = applyFilter(criteria)
        )
    }

    private fun applyFilter(criteria: CriteriaDefinition?): Criteria {
        return if (criteria == null) {
            filter
        } else {
            filter.and(criteria)
        }
    }
}
