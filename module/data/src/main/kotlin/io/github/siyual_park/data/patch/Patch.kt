package io.github.siyual_park.data.patch

interface Patch<T> {
    fun apply(entity: T): T

    companion object {
        fun <T> from(patch: (entity: T) -> T) = object : Patch<T> {
            override fun apply(entity: T): T = patch(entity)
        }

        fun <T> with(patch: (entity: T) -> Unit) = object : Patch<T> {
            override fun apply(entity: T): T {
                patch(entity)
                return entity
            }
        }
    }
}

fun <T> Patch<T>.async(): SuspendPatch<T> {
    return SuspendPatch.from { apply(it) }
}
