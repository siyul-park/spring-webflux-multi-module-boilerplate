package io.github.siyual_park.data.patch

interface AsyncPatch<T> {
    suspend fun apply(entity: T): T

    companion object {
        fun <T> from(patch: suspend (entity: T) -> T) = object : AsyncPatch<T> {
            override suspend fun apply(entity: T): T = patch(entity)
        }

        fun <T> with(patch: suspend(entity: T) -> Unit) = object : AsyncPatch<T> {
            override suspend fun apply(entity: T): T {
                patch(entity)
                return entity
            }
        }
    }
}
