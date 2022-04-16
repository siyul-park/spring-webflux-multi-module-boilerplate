package io.github.siyual_park.data

abstract class Entity<ID> {
    abstract var id: ID

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Entity<*>

        return id == other.id
    }
}
