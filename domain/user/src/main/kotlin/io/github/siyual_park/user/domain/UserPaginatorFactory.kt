package io.github.siyual_park.user.domain

import io.github.siyual_park.data.expansion.columnName
import io.github.siyual_park.reader.pagination.OffsetPaginator
import io.github.siyual_park.reader.pagination.OffsetPaginatorAdapter
import io.github.siyual_park.user.entity.User
import io.github.siyual_park.user.repository.UserRepository
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.query.CriteriaDefinition
import org.springframework.stereotype.Component

@Component
class UserPaginatorFactory(
    private val userRepository: UserRepository,
) {
    fun create(
        criteria: CriteriaDefinition? = null,
        sort: Sort? = null
    ): OffsetPaginator<User> {
        return OffsetPaginatorAdapter(
            userRepository,
            sort = sort ?: Sort.by(Sort.Direction.DESC, columnName(User::createdAt)),
            criteria = criteria
        )
    }
}
