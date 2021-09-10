package io.github.siyual_park.data

import io.github.siyual_park.data.annoration.GeneratedValue
import org.springframework.data.annotation.Id

abstract class IdEntity<T, ID> : Cloneable<T> {
    @Id
    @GeneratedValue
    var id: ID? = null
}

fun <T : IdEntity<T, ID>, ID> IdEntity<T, ID>.copyDefaultColumn(entity: T): T {
    entity.id = id

    return entity
}
