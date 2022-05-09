package io.github.siyual_park.data.repository.r2dbc.repository.r2dbc

import io.github.siyual_park.data.repository.r2dbc.R2DBCRepositoryBuilder
import io.github.siyual_park.data.repository.r2dbc.entity.Person
import io.github.siyual_park.ulid.ULID
import org.springframework.data.relational.core.query.Criteria

class FilteredRepositoryTest : R2DBCRepositoryTestHelper(
    repositories = {
        listOf(
            R2DBCRepositoryBuilder<Person, ULID>(it.entityOperations, Person::class)
                .set { Criteria.empty() }
                .build()
        )
    }
)
