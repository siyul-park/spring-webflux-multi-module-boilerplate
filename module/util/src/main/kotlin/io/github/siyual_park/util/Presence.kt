package io.github.siyual_park.util

sealed class Presence<T : Any?> {
    data class Exist<T : Any?>(val value: T) : Presence<T>()
    class Empty<T : Any?> : Presence<T>()

    companion object {
        fun <T : Any> ofNullable(value: T?): Presence<T> {
            return if (value != null) {
                Exist(value)
            } else {
                Empty()
            }
        }
    }

    fun <U : Any?> flatMap(func: (T) -> Presence<U>): Presence<U> {
        return when (this) {
            is Exist -> func(this.value)
            is Empty -> Empty()
        }
    }

    fun orElseGet(func: () -> T): T {
        return when (this) {
            is Exist -> this.value
            is Empty -> func()
        }
    }
}
