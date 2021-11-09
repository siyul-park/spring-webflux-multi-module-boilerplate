package io.github.siyual_park.mapper

import java.io.Serializable
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.Objects

abstract class TypeReference<T> : Serializable {
    val type: Type

    init {
        val superClass: Type = javaClass.genericSuperclass
        type = (superClass as ParameterizedType).actualTypeArguments[0]
    }

    override fun hashCode(): Int {
        return Objects.hash(type)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TypeReference<*>

        return type == other.type
    }
}
