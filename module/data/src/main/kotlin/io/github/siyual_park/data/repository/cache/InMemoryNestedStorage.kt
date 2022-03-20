package io.github.siyual_park.data.repository.cache

import com.google.common.cache.CacheBuilder

@Suppress("UNCHECKED_CAST")
class InMemoryNestedStorage<T : Any, ID : Any>(
    private val cacheBuilder: CacheBuilder<ID, T>,
    private val idExtractor: Extractor<T, ID>
) : NestedStorage<T, ID>, Storage<T, ID> by InMemoryStorage(cacheBuilder, idExtractor) {
    override val parent: NestedStorage<T, ID>? = null

    override fun diff(): Pair<Set<T>, Set<ID>> {
        return setOf<T>() to setOf()
    }

    override fun fork(): NestedStorage<T, ID> {
        return InMemoryNestedStorageNode(
            idExtractor,
            this
        ).also {
            getExtractors().forEach { (name, extractor) ->
                it.createIndex(name, extractor as Extractor<T, Any>)
            }
        }
    }

    override fun join(storage: NestedStorage<T, ID>) {
        val (created, removed) = storage.diff()
        storage.clear()

        removed.forEach {
            remove(it)
        }
        created.forEach {
            put(it)
        }
    }
}
