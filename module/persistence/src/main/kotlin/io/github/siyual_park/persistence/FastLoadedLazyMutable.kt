package io.github.siyual_park.persistence

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST")
class FastLoadedLazyMutable<T : Any>(
    private var value: T
) : LazyMutable<T> {
    private var commands = mutableMapOf<KMutableProperty1<T, Any?>, Any?>()

    override operator fun <V : Any?> get(property: KProperty1<T, V>): V {
        if (property is KMutableProperty1<T, V>) {
            val command = commands[property as KMutableProperty1<T, Any?>]
            if (commands.contains(property) || command != null) {
                return command as V
            }
        }

        return property.get(value)
    }

    override operator fun <V : Any?> set(property: KMutableProperty1<T, V>, value: V) {
        commands[property as KMutableProperty1<T, Any?>] = value
    }

    fun raw(value: T) {
        this.value = value
    }

    override fun raw(): T {
        return value
    }

    override fun isUpdated(): Boolean {
        return commands.isNotEmpty()
    }

    override fun clear() {
        commands = mutableMapOf()
    }

    override fun checkout(): Map<KMutableProperty1<T, Any?>, Any?> {
        val updateCommands = commands
        commands = mutableMapOf()
        return updateCommands
    }
}

fun <T : Any> LazyMutable.Companion.from(value: T) = FastLoadedLazyMutable(value)
