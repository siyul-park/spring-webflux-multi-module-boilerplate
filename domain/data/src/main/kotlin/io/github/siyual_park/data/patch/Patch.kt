package io.github.siyual_park.data.patch

interface Patch<T> {
    fun apply(entity: T): T

    companion object {
        fun <T> from(patch: (entity: T) -> T) = object : Patch<T> {
            override fun apply(entity: T) = patch(entity)
        }
    }
}

fun <T> Patch<T>.async(): AsyncPatch<T> {
    return AsyncPatch.from { apply(it) }
}
