package io.github.siyual_park.data.aggregation

import io.github.siyual_park.data.repository.QueryRepository
import org.apache.commons.collections4.keyvalue.MultiKey
import org.apache.commons.collections4.map.MultiKeyMap
import kotlin.reflect.KClass

class FetchContextProvider {
    private val contexts = MultiKeyMap<Any, FetchContext<*>>()

    fun <T : Any> get(repository: QueryRepository<T, *>, clazz: KClass<T>): FetchContext<T> {
        @Suppress("UNCHECKED_CAST")
        return contexts.getOrPut(MultiKey(repository, clazz)) {
            FetchContext(repository, clazz)
        } as FetchContext<T>
    }
}

inline fun <reified T : Any> FetchContextProvider.get(repository: QueryRepository<T, *>) = this.get(repository, T::class)
