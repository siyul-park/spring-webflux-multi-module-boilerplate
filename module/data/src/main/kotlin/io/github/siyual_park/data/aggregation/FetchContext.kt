package io.github.siyual_park.data.aggregation

import io.github.siyual_park.data.repository.QueryableRepository
import org.apache.commons.collections4.keyvalue.MultiKey
import org.apache.commons.collections4.map.MultiKeyMap
import kotlin.reflect.KClass

class FetchContext {
    private val contexts = MultiKeyMap<Any, AggregateContext<*>>()

    fun <T : Any> get(repository: QueryableRepository<T, *>, clazz: KClass<T>): AggregateContext<T> {
        @Suppress("UNCHECKED_CAST")
        return contexts.getOrPut(MultiKey(repository, clazz)) {
            AggregateContext(repository, clazz)
        } as AggregateContext<T>
    }
}

inline fun <reified T : Any> FetchContext.get(repository: QueryableRepository<T, *>) = this.get(repository, T::class)
