package io.github.siyual_park.persistence

interface PersistenceSynchronization {
    suspend fun beforeClear() {}
    suspend fun afterClear() {}

    suspend fun beforeSync() {}
    suspend fun afterSync() {}
}
