package io.github.siyual_park.presentation.project

data class Projection<T : Any>(
    val value: T,
    val node: ProjectNode
)
