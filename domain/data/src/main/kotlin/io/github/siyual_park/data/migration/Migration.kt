package io.github.siyual_park.data.migration

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate

interface Migration {
    suspend fun up(entityTemplate: R2dbcEntityTemplate)
    suspend fun down(entityTemplate: R2dbcEntityTemplate)
}
