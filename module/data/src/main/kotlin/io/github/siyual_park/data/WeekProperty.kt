package io.github.siyual_park.data

fun interface WeekProperty<T : Any, KEY : Any?> {
    fun get(entity: T): KEY
}
