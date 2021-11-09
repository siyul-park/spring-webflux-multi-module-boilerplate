package io.github.siyual_park.data.callback

import kotlin.reflect.KClass

interface AfterSaveCallback<T : Any> {
    val clazz: KClass<T>

    suspend fun onAfterSave(entity: T)
}
