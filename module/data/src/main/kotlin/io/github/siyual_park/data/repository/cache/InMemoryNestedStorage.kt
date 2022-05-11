package io.github.siyual_park.data.repository.cache

@Suppress("UNCHECKED_CAST")
class InMemoryNestedStorage<T : Any, ID : Any>(
    private val delegator: Storage<T, ID>,
) : NestedStorage<T, ID>, Storage<T, ID> by delegator {
    override val parent: NestedStorage<T, ID>? = null

    override fun diff(): Pair<Set<T>, Set<ID>> {
        return setOf<T>() to setOf()
    }

    override suspend fun fork(): NestedStorage<T, ID> {
        return InMemoryNestedStorageNode(
            this
        ).also {
            getExtractors().forEach { (name, extractor) ->
                it.createIndex(name, extractor as Extractor<T, Any>)
            }
        }
    }

    override suspend fun merge(storage: NestedStorage<T, ID>) {
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
