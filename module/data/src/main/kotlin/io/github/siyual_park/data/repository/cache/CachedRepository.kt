package io.github.siyual_park.data.repository.cache

import io.github.siyual_park.data.repository.Repository

interface CachedRepository<T : Any, ID : Any> : Repository<T, ID> {
    val storageManager: StorageManager<T, ID>
}
