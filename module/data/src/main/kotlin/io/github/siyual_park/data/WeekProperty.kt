package io.github.siyual_park.data

interface WeekProperty<T : Any, KEY : Any?> {
    fun get(entity: T): KEY
}
