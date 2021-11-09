package io.github.siyual_park.data.repository.cache

import io.github.siyual_park.data.repository.Repository

class CacheLoaderAdapter<T : Any, Key : Any>(
    private val repository: Repository<T, Key>,
) : CacheLoader<T, Key>() {
    override suspend fun loadByKey(key: Key): T? {
        return repository.findById(key)
    }
}
