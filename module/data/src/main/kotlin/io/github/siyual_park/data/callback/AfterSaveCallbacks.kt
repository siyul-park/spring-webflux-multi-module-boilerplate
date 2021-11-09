package io.github.siyual_park.data.callback

import org.springframework.stereotype.Component

@Suppress("UNCHECKED_CAST")
@Component
class AfterSaveCallbacks {
    private val callbacks = mutableMapOf<Class<*>, MutableList<AfterSaveCallback<*>>>()

    fun <T : Any> register(callback: AfterSaveCallback<T>) {
        callbacks.getOrPut(callback.clazz.java) { mutableListOf() }.add(callback)
    }

    suspend fun <T : Any> onAfterSave(entity: T) {
        callbacks[entity.javaClass]
            ?.let { callbacks ->
                callbacks.forEach {
                    it as AfterSaveCallback<T>
                    it.onAfterSave(entity)
                }
            }
    }
}
