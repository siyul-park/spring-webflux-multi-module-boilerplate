package io.github.siyual_park.user.repository

import io.github.siyual_park.data.repository.r2dbc.CachedR2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.github.siyual_park.user.entity.User
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.stereotype.Repository

@Repository
class UserRepository(
    entityOperations: R2dbcEntityOperations
) : R2DBCRepository<User, Long> by CachedR2DBCRepository.of(
    entityOperations,
    User::class
)
