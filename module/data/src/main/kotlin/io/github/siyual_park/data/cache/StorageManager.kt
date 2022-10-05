package io.github.siyual_park.data.cache

import org.springframework.stereotype.Component
import java.util.Collections

@Component
class StorageManager {
    private val storages = Collections.synchronizedMap(mutableMapOf<String, Storage<*, *>>())

    fun put(key: String, storage: Storage<*, *>) {
        storages[key] = storage
    }

    fun get(key: String): Storage<*, *>? {
        return storages[key]
    }

    fun remove(key: String): Storage<*, *>? {
        return storages.remove(key)
    }

    suspend fun status(): Map<String, Status> {
        return storages.map { (key, value) -> key to value.status() }.toMap()
    }
}
