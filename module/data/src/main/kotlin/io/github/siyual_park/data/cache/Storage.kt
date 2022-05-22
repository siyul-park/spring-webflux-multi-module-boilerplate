package io.github.siyual_park.data.cache

import io.github.siyual_park.data.annotation.Key
import io.github.siyual_park.data.expansion.columnName
import io.github.siyual_park.data.repository.Extractor
import org.springframework.data.annotation.Id
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

interface Storage<ID : Any, T : Any> {
    suspend fun <KEY : Any> createIndex(name: String, extractor: Extractor<T, KEY>)
    suspend fun removeIndex(name: String)
    suspend fun containsIndex(name: String): Boolean

    suspend fun getExtractors(): Map<String, Extractor<T, *>>

    suspend fun <KEY : Any> getIfPresent(index: String, key: KEY): T?
    suspend fun <KEY : Any> getIfPresent(index: String, key: KEY, loader: suspend () -> T?): T?

    suspend fun getIfPresent(id: ID): T?
    suspend fun getIfPresent(id: ID, loader: suspend () -> T?): T?

    suspend fun remove(id: ID)

    suspend fun delete(entity: T)
    suspend fun put(entity: T)

    suspend fun entries(): Set<Pair<ID, T>>

    suspend fun clear()
}

suspend fun <ID : Any, T : Any> Storage<ID, T>.createIndexes(clazz: KClass<T>, indexName: (KProperty1<T, *>) -> String = { it.name }) {
    val indexes = mutableMapOf<String, MutableList<KProperty1<T, *>>>()
    clazz.memberProperties.forEach {
        val index = it.annotations.find { it is Key } as? Key ?: return@forEach
        indexes.getOrPut(index.name.ifEmpty { indexName(it) }) { mutableListOf() }
            .add(it)
    }
    clazz.memberProperties.forEach {
        if (it.javaField?.annotations?.find { it is Id } == null) return@forEach
        indexes.getOrPut(indexName(it)) { mutableListOf() }
            .add(it)
    }

    indexes.forEach { (_, properties) ->
        val sortedProperties = properties.map { it to columnName(it) }.sortedBy { (_, columnName) -> columnName }
        createIndex(
            sortedProperties.joinToString(" ") { (_, columnName) -> columnName },
            object : Extractor<T, Any> {
                override fun getKey(entity: T): Any {
                    val key = ArrayList<Any?>()
                    sortedProperties.forEach { (property, _) -> key.add(property.get(entity)) }
                    return key
                }
            }
        )
    }
}
