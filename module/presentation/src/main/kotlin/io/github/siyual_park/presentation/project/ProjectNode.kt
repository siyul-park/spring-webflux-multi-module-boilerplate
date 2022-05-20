package io.github.siyual_park.presentation.project

import java.util.Optional
import kotlin.reflect.KProperty1

sealed class ProjectNode {
    object Leaf : ProjectNode() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            return true
        }

        override fun hashCode(): Int {
            return 0
        }
    }

    class Stem(
        value: MutableMap<String, ProjectNode> = mutableMapOf()
    ) : ProjectNode(), MutableMap<String, ProjectNode> by value {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            other as Stem
            return entries == other.entries
        }

        override fun hashCode(): Int {
            return System.identityHashCode(this)
        }
    }

    companion object {
        fun from(value: Collection<String>): ProjectNode {
            if (value.isEmpty()) {
                return Leaf
            }

            val stem = Stem()
            val tokenized = value.map { it.split(".") }

            tokenized.forEach { tokens ->
                var current = stem
                for (i in tokens.indices) {
                    val token = tokens[i]

                    if (i < tokens.size - 1) {
                        var local = current[token] ?: Stem()
                        if (local is Leaf) {
                            local = Stem()
                        }
                        current[token] = local
                        current = local as Stem
                    } else {
                        current[token] = current[token] ?: Leaf
                    }
                }
            }

            return stem
        }
    }
}

inline fun <T : Any, V : Any> ProjectNode.project(property: KProperty1<T, Optional<V>?>, value: (ProjectNode) -> V?): Optional<V>? {
    if (this is ProjectNode.Leaf) {
        return Optional.ofNullable(value(ProjectNode.Leaf))
    } else if (this is ProjectNode.Stem) {
        val current = this[property.name] ?: return null
        return Optional.ofNullable(value(current))
    }
    return null
}
