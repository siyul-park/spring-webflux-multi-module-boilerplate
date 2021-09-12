package io.github.siyual_park.data.repository.in_memory.generator

interface IdGenerator<ID> {
    fun generate(): ID
}
