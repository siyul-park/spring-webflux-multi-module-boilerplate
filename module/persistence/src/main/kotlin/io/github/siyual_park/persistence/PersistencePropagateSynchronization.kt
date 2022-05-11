package io.github.siyual_park.persistence

class PersistencePropagateSynchronization<T : Any, ID : Any>(
    private val persistence: Persistence<T, ID>,
) : PersistenceSynchronization {
    override suspend fun beforeSync() {
        persistence.sync()
    }

    override suspend fun beforeClear() {
        persistence.clear()
    }
}
