package io.github.siyual_park.data

import io.github.siyual_park.data.annoration.GeneratedValue
import org.springframework.data.relational.core.mapping.Column
import java.time.Instant

abstract class TimeableEntity<T, ID> : IdEntity<T, ID>() {
    @Column("created_at")
    @GeneratedValue
    var createdAt: Instant? = null

    @Column("updated_at")
    @GeneratedValue
    var updatedAt: Instant? = null
}

fun <T : TimeableEntity<T, ID>, ID> TimeableEntity<T, ID>.copyDefaultColumn(entity: T): T {
    entity.id = id
    entity.createdAt = createdAt
    entity.updatedAt = updatedAt

    return entity
}
