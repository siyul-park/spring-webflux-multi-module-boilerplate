package io.github.siyual_park.persistence

import io.github.siyual_park.data.patch.Patch
import java.util.concurrent.Semaphore
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST")
class LazyLoadedLoadedLazyMutable<T : Any>(
    private var loader: () -> T
) : LazyMutable<T> {
    private var cachedValue: T? = null
    private val semaphore = Semaphore(1)

    private var commands = mutableMapOf<KMutableProperty1<T, Any?>, Any?>()

    override operator fun <V : Any?> get(property: KProperty1<T, V>): V {
        if (property is KMutableProperty1<T, V>) {
            val command = commands[property as KMutableProperty1<T, Any?>]
            if (commands.contains(property) || command != null) {
                return command as V
            }
        }

        return property.get(raw())
    }

    override operator fun <V : Any?> set(property: KMutableProperty1<T, V>, value: V) {
        commands[property as KMutableProperty1<T, Any?>] = value
    }

    override fun raw(): T {
        return if (cachedValue == null) {
            semaphore.acquire()
            val value = loader()
            cachedValue = value
            semaphore.release()
            value
        } else {
            cachedValue!!
        }
    }

    override fun isUpdated(): Boolean {
        return commands.isNotEmpty()
    }

    override fun clear() {
        commands = mutableMapOf()
    }

    override fun toPatch(): Patch<T> {
        return Patch.with { newone ->
            val updateCommands = commands
            commands = mutableMapOf()

            updateCommands.forEach { (property, command) ->
                property.set(newone, command)
            }

            cachedValue = newone
        }
    }
}

fun <T : Any> LazyMutable.Companion.from(loader: () -> T) = LazyLoadedLoadedLazyMutable(loader)
