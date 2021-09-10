package io.github.siyual_park.data

import io.github.siyual_park.data.annoration.GeneratedValue
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.time.Instant

abstract class BaseEntity<T> : Cloneable<T> {
    @Id
    @GeneratedValue
    var id: Long? = null

    @Column("created_at")
    @GeneratedValue
    var createdAt: Instant? = null

    @Column("updated_at")
    @GeneratedValue
    var updatedAt: Instant? = null
}

fun <T : BaseEntity<T>> BaseEntity<T>.copyDefaultColumn(entity: T): T {
    entity.id = id
    entity.createdAt = createdAt
    entity.updatedAt = updatedAt

    return entity
}
