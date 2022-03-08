package io.github.siyual_park.data

import io.github.siyual_park.data.annotation.GeneratedValue
import org.springframework.data.annotation.Id

abstract class IdEntity<T, ID> {
    @Id
    @GeneratedValue
    var id: ID? = null

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IdEntity<*, *>

        return id == other.id
    }
}
